package de.weimarnetz.registrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class NodeResponse {

    String message;
    int status;
    @JsonProperty("result")
    Node node;
}
