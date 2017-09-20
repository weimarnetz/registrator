package de.weimarnetz.registrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class NodeResponse {

    private String message;
    private int status;
    @JsonProperty("result")
    private Node node;
}
