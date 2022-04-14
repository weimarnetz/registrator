package de.weimarnetz.registrator.model

import com.fasterxml.jackson.annotation.JsonProperty

data class NodeResponse(
    val message: String,
    val status: Int,

    @JsonProperty("result")
    var node: Node? = null
)