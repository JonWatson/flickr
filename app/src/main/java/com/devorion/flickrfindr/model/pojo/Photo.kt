package com.devorion.flickrfindr.model.pojo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@Entity(tableName = "bookmarkedPhotos")
data class Photo @JsonCreator constructor(
    @PrimaryKey @ColumnInfo(name = "id")
    @param:JsonProperty("id") val id: String,
    @ColumnInfo(name = "title")
    @param:JsonProperty("title") val title: String,
    @ColumnInfo(name = "farm")
    @param:JsonProperty("farm") val farm: String,
    @ColumnInfo(name = "server")
    @param:JsonProperty("server") val server: String,
    @ColumnInfo(name = "secret")
    @param:JsonProperty("secret") val secret: String,
    @ColumnInfo(name = "width_s")
    @param:JsonProperty("width_s") val widthS: Float,
    @ColumnInfo(name = "height_s")
    @param:JsonProperty("height_s") val heightS: Float,
    @ColumnInfo(name = "width_m")
    @param:JsonProperty("width_m") val widthM: Float,
    @ColumnInfo(name = "height_m")
    @param:JsonProperty("height_m") val heightM: Float,
    @ColumnInfo(name = "width_L")
    @param:JsonProperty("width_l") val widthL: Float,
    @ColumnInfo(name = "height_l")
    @param:JsonProperty("height_l") val heightL: Float
) : Serializable {

    // Knowing the Aspect Ratio of the image is essential to the desired design
    @Ignore
    val aspectRatio = when {
        widthS != 0f && heightS != 0f -> widthS / heightS
        widthM != 0f && heightM != 0f -> widthM / heightM
        widthL != 0f && heightL != 0f -> widthL / heightL
        else -> 1f
    }

    // Note that we are hardcoding "_z" as the image quality(max side = 640px) based on the project
    // With a larger project we would likely allow the caller to specify the size image
    fun getImageUrl() =
        "https://farm$farm.staticflickr.com/$server/${id}_${secret}_z.jpg"
}