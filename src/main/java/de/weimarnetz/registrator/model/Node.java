package de.weimarnetz.registrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
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
    private long key;
    @Min(2)
    @Max(1000)
    private int number;
    private String mac;
    @JsonProperty("created_at")
    private long createdAt;
    @JsonProperty("last_seen")
    private long lastSeen;
    private String location;
    private String network;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String pass;

}
