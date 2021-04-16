package de.weimarnetz.registrator.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class NodesResponse {

    String message;
    int status;
    @JsonProperty("result")
    List<Node> node;
}
