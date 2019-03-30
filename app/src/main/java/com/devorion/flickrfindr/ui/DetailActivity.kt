package com.devorion.flickrfindr.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.devorion.flickrfindr.App
import com.devorion.flickrfindr.R
import com.devorion.flickrfindr.model.pojo.Photo
import com.devorion.flickrfindr.util.ImageLoader
import kotlinx.android.synthetic.main.photo_item.*
import javax.inject.Inject

class DetailActivity : AppCompatActivity() {

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        (application as App).appComponent.inject(this)

        with(intent.extras?.getSerializable(EXTRA_PHOTO) as Photo) {
            imageLoader.loadPhotoIntoImage(this, image, resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
        }
    }

    companion object {
        const val EXTRA_PHOTO = "photo"
    }
}