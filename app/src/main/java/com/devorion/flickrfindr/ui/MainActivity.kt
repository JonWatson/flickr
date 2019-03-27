package com.devorion.flickrfindr.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SearchRecentSuggestionsProvider
import android.os.Bundle
import android.os.Handler
import android.provider.SearchRecentSuggestions
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.devorion.flickrfindr.App
import com.devorion.flickrfindr.R
import com.devorion.flickrfindr.model.NetworkViewModel
import com.devorion.flickrfindr.di.ViewModelFactory
import com.devorion.flickrfindr.model.state.Status
import com.devorion.flickrfindr.ui.list.GridSpacingItemDecoration
import com.devorion.flickrfindr.ui.list.GridSpanSizeLookup
import com.devorion.flickrfindr.ui.list.PhotosAdapter
import com.devorion.flickrfindr.util.*
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private val LOG: Logger = Logger.getLogger(MainActivity::class.java)

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    @Inject
    lateinit var bookmarkManager: BookmarkManager
    @Inject
    lateinit var imageLoader: ImageLoader
    @Inject
    lateinit var connectionMonitor: ConnectionMonitor

    private lateinit var viewModel: NetworkViewModel
    private val startActivityThrottler = StartActivityThrottler()

    private var restoreOpenSearch = false
    private var currentSearchText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (application as App).appComponent.inject(this)
        setSupportActionBar(toolbar)

        val adapter = initializeAdapter()
        initializeViewModel(adapter)
        initializeRefresh(adapter)
        initializeOfflineView()

        savedInstanceState?.let {
            restoreOpenSearch = it.getBoolean(EXTRA_OPEN_SEARCH)
            currentSearchText = it.getString(EXTRA_SEARCH_TEXT, "")
        }

        bookmarkManager.bookmarkServiceState.observe(this, Observer {
            BookmarkSnackbarDelegate().showBookmarkSnackbar(root, it)
        })
    }

    private fun initializeAdapter(): PhotosAdapter {
        val itemSpanSize = resources.getInteger(R.integer.photo_columns)
        val totalSpanSize = resources.getInteger(R.integer.lcm_photo_columns)
        val adapter = PhotosAdapter(
            imageLoader,
            bookmarkManager,
            itemSpanSize,
            startActivityThrottler
        ) {
            viewModel.retryLastFailedPage()
        }

        photo_list.apply {
            layoutManager =
                GridLayoutManager(
                    context,
                    totalSpanSize
                ).apply {
                    spanSizeLookup = GridSpanSizeLookup(adapter, itemSpanSize, totalSpanSize)
                }
            addItemDecoration(
                GridSpacingItemDecoration(
                    resources.getDimensionPixelOffset(
                        R.dimen.grid_padding
                    )
                )
            )

            this.adapter = adapter
        }
        return adapter
    }

    private fun initializeViewModel(adapter: PhotosAdapter) {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[NetworkViewModel::class.java]

        viewModel.photos.observe(this, Observer {
            initial_loading.visibility = View.GONE
            // Handle empty view
            if (it.size == 0 && viewModel.networkState.value?.status != Status.FAILED) {
                empty_text_view.visibility = View.VISIBLE
                photo_list.visibility = View.GONE
                empty_text_view.text = getString(R.string.empty_view_no_results, currentSearchText)
            } else {
                empty_text_view.visibility = View.GONE
                photo_list.visibility = View.VISIBLE
            }

            adapter.submitList(it)
        })

        viewModel.networkState.observe(this, Observer {
            var hideSwipeToRefresh = true
            var hideNetworkStatusCard = false
            var hideInitialLoading = true
            when {
                it.status == Status.SUCCESS -> {
                    hideNetworkStatusCard = true
                }
                it.isInitialLoad && it.status == Status.LOADING -> {
                    hideSwipeToRefresh = !swipe_refresh.isRefreshing
                    hideInitialLoading = swipe_refresh.isRefreshing
                    hideNetworkStatusCard = true
                }
                else -> {

                }
            }

            empty_text_view.visibility = View.GONE
            if (hideInitialLoading) {
                initial_loading.visibility = View.GONE
            } else {
                initial_loading.visibility = View.VISIBLE
            }
            if (hideSwipeToRefresh) {
                swipe_refresh.isRefreshing = false
            }
            if (hideNetworkStatusCard) {
                adapter.removeNetworkStatusCard()
            } else {
                adapter.addOrUpdateNetworkStatusCard(it)
            }
        })
    }

    private fun initializeOfflineView() {
        connectionMonitor.networkLiveData.observe(
            this, Observer {
                offline_bar.visibility =
                    if (it == ConnectionMonitor.ConnectionStatus.DISCONNECTED) View.VISIBLE else View.GONE
            })

        offline_bar.setOnClickListener {
            if (startActivityThrottler.okToStartActivity()) {
                startActivity(Intent(this, BookmarkActivity::class.java))
            }
        }
    }

    private fun initializeRefresh(adapter: PhotosAdapter) {
        swipe_refresh.setOnRefreshListener {
            if (adapter.itemCount == 0) {
                swipe_refresh.isRefreshing = false
            } else {
                photo_list.scrollToPosition(0)
                adapter.submitList(null)
                if (!viewModel.refresh()) {
                    swipe_refresh.isRefreshing = false
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Show the appBar(and thus SearchView) if we resume with the SearchView focused
        if (getSearchView()?.hasFocus() == true) {
            appbar.setExpanded(true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_OPEN_SEARCH, getSearchMenuItem()?.isActionViewExpanded == true)
        outState.putString(EXTRA_SEARCH_TEXT, currentSearchText)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.main, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.action_search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setOnQueryTextFocusChangeListener { _, focused ->
                this@MainActivity.restoreOpenSearch = focused
            }
        }

        // Showcase the Search MenuItem.  The post() is necessary to wait for the ActionView to be added
        // to the Toolbar layout.  Perhaps an alternative Showcase library would handle this more gracefully
        Handler().post {
            MaterialShowcaseView.Builder(this)
                .setTarget(findViewById(R.id.action_search))
                .setContentTextColor(ContextCompat.getColor(this, R.color.dark_gray))
                .setDismissTextColor(ContextCompat.getColor(this, R.color.dark_gray))
                .setMaskColour(ContextCompat.getColor(this, R.color.light_gray))
                .setDismissOnTouch(true)
                .setFadeDuration(800)
                .setDelay(350)
                .setContentText(getString(R.string.showcase_message))
                .singleUse("one-shot")
                .show()
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // Handle some state restoration for SearchView
        if (restoreOpenSearch) {
            // SearchView being open doesn't persist through rotation
            menu.findItem(R.id.action_search).expandActionView()
        }

        (menu.findItem(R.id.action_search).actionView as SearchView).apply {
            setOnQueryTextListener(object :
                SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    currentSearchText = newText ?: ""
                    return false
                }
            })
            setQuery(currentSearchText, false)

            return super.onPrepareOptionsMenu(menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_suggestions -> {
                SearchRecentSuggestions(
                    this,
                    SEARCH_AUTHORITY,
                    SEARCH_MODE
                ).clearHistory()

                true
            }
            R.id.action_view_bookmarks -> {
                if (startActivityThrottler.okToStartActivity()) {
                    startActivity(Intent(this, BookmarkActivity::class.java))
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Handles all searches made from voice, text entry, and suggestion clicks
    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH) {
            val searchText = intent.getStringExtra(SearchManager.QUERY)
            if (searchText.isNotEmpty()) {
                SearchRecentSuggestions(
                    this,
                    SEARCH_AUTHORITY,
                    SEARCH_MODE
                ).saveRecentQuery(searchText, null)

                doSearch(searchText)
            }
        }
    }

    private fun doSearch(searchText: String) {
//        if (connectionMonitor.networkLiveData.value == ConnectionMonitor.ConnectionStatus.DISCONNECTED) {
//            showToast(resources.getString(R.string.offline_error_message))
//            return
//        }

        val trimmedQuery = searchText.trim()
        if (viewModel.updateSearchText(trimmedQuery)) {
            // New Search text(and DataSource), clear scroll position or LayoutManager
            // will try to restore after the new data arrives
            photo_list.scrollToPosition(0)
            (photo_list.adapter as? PhotosAdapter)?.submitList(null)
        }

        // Voice or Suggestion clicks should update SearchView text
        getSearchView()?.apply {
            setQuery(searchText, false)
            clearFocus()    // Clear SearchView focus to avoid showing suggestion popup
            closeKeyboard()
        }

        currentSearchText = trimmedQuery    // For Rotation
    }

    private fun closeKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(photo_list.windowToken, 0)
    }

    private fun getSearchMenuItem(): MenuItem? {
        return toolbar.menu.findItem(R.id.action_search)
    }

    private fun getSearchView(): SearchView? {
        return getSearchMenuItem()?.actionView as SearchView?
    }

    private fun showToast(msg: String?) {
        Toast.makeText(
            this@MainActivity,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }

    // Search View suggestions, the way Android meant it be
    class SuggestionProvider : SearchRecentSuggestionsProvider() {
        init {
            setupSuggestions(
                SEARCH_AUTHORITY,
                SEARCH_MODE
            )
        }
    }

    companion object {
        const val EXTRA_OPEN_SEARCH = "open"
        const val EXTRA_SEARCH_TEXT = "searchText"
        const val SEARCH_AUTHORITY = "com.devorion.flickrfindr.SuggestionProvider"
        const val SEARCH_MODE: Int = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }
}