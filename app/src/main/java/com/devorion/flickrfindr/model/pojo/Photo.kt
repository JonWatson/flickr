package com.devorion.flickrfindr.model.pojo

import androidx.room.ColumnInfo
import androidx.room.Entity
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
    @param:JsonProperty("secret") val secret: String
) : Serializable {

    // Note that we are hardcoding "_z" as the image quality(max side = 640px) based on the project
    // With a larger project we would likely allow the caller to specify the size image
    fun getImageUrl() =
        "https://farm$farm.staticflickr.com/$server/${id}_${secret}_z.jpg"
}