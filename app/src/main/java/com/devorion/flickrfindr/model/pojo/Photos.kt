package com.devorion.flickrfindr.model.pojo

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Photos @JsonCreator constructor(
    @param:JsonProperty("page") val page: Int,
    @param:JsonProperty("pages") val numPages: Int,
    @param:JsonProperty("perpage") val pageSize: Int,
    @param:JsonProperty("total") val total: Int,
    @param:JsonProperty("photo") val photoList: List<Photo>
)