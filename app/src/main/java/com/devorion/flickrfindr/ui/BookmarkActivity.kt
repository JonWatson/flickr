package com.devorion.flickrfindr.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.devorion.flickrfindr.App
import com.devorion.flickrfindr.R
import com.devorion.flickrfindr.model.bookmarks.BookmarkPhotoDatabase
import com.devorion.flickrfindr.model.bookmarks.BookmarkedViewModel
import com.devorion.flickrfindr.ui.list.GridSpacingItemDecoration
import com.devorion.flickrfindr.ui.list.GridSpanSizeLookup
import com.devorion.flickrfindr.ui.list.PhotosAdapter
import com.devorion.flickrfindr.util.*
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class BookmarkActivity : AppCompatActivity() {

    private val LOG: Logger = Logger.getLogger(BookmarkActivity::class.java)

    @Inject
    lateinit var imageLoader: ImageLoader
    @Inject
    lateinit var bookmarkPhotoDatabase: BookmarkPhotoDatabase
    @Inject
    lateinit var bookmarkManager: BookmarkManager

    private lateinit var viewModel: BookmarkedViewModel
    private val startActivityThrottler = StartActivityThrottler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)

        (application as App).appComponent.inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.activity_title_bookmarks)

        val adapter = initializeAdapter()
        initializeViewModel(adapter)
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
            // no-op
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

            itemAnimator = null
            this.adapter = adapter
        }

        bookmarkManager.bookmarkServiceState.observe(this, Observer{
            BookmarkSnackbarDelegate().showBookmarkSnackbar(root, it)
        })

        return adapter
    }

    private fun initializeViewModel(adapter: PhotosAdapter) {
        viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return BookmarkedViewModel(bookmarkPhotoDatabase.bookmarkedPhotoDao()) as T
            }
        })[BookmarkedViewModel::class.java]

        viewModel.bookmarkedPhotos.observe(this, Observer {
            adapter.submitList(it)

            if (it.size == 0) {
                photo_list.visibility = View.GONE
                empty_text_view.visibility = View.VISIBLE
                empty_text_view.text = getString(R.string.empty_view_no_bookmarks)
            } else {
                photo_list.visibility = View.VISIBLE
                empty_text_view.visibility = View.GONE
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}