package com.devorion.flickrfindr.model.pojo

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SearchResponse @JsonCreator constructor(
    @param:JsonProperty("photos") val photos: Photos,
    @param:JsonProperty("stat") val status: String
)