package de.weimarnetz.registrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@Builder
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"number", "network"}),
        @UniqueConstraint(columnNames = {"network", "mac"})
})
public class Node {

    @Tolerate
    Node() {}

    @Id
    @GeneratedValue
    long key;
    @Min(2)
    @Max(1000)
    int number;
    String mac;
    @JsonProperty("created_at")
    long createdAt;
    @JsonProperty("last_seen")
    long lastSeen;
    String location;
    String network;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String pass;

}
