package de.weimarnetz.registrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class NodesResponse {

    private String message;
    private int status;
    @JsonProperty("result")
    private List<Node> node;
}
