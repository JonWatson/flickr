package com.devorion.flickrfindr.ui

import android.os.Bundle
import android.widget.FrameLayout
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
            // Our ImageView's aspect ratio is always based off the image's aspect ratio
            image_container.tag = this.aspectRatio

            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            val screenAspectRatio = screenWidth.toFloat() / screenHeight

            // Determine if the ImageView will be bounded by screen width or height.  Extremely wide images need
            // to be bounded by the width, tall images by the height
            val useWidthToDetermineAspectRatio = this.aspectRatio > screenAspectRatio

            // AspectRatioFrameLayout requires bounding width or height by using MATCH_PARENT
            image_container.layoutParams.height =
                if (useWidthToDetermineAspectRatio) FrameLayout.LayoutParams.WRAP_CONTENT else FrameLayout.LayoutParams.MATCH_PARENT
            image_container.layoutParams.width =
                if (useWidthToDetermineAspectRatio) FrameLayout.LayoutParams.MATCH_PARENT else FrameLayout.LayoutParams.WRAP_CONTENT

            val width = if (useWidthToDetermineAspectRatio) screenWidth else (screenWidth / this.aspectRatio).toInt()
            val height = if (useWidthToDetermineAspectRatio) (screenWidth * this.aspectRatio).toInt() else screenHeight

            imageLoader.loadPhotoIntoImage(this, image, width, height)
        }
    }

    companion object {
        const val EXTRA_PHOTO = "photo"
    }
}