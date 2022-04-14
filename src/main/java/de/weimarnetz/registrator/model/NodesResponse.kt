package de.weimarnetz.registrator.model

import com.fasterxml.jackson.annotation.JsonProperty


data class NodesResponse(
    val message: String,
    val status: Int,

    @JsonProperty("result")
    val node: List<Node>? = null
)