package de.weimarnetz.registrator.model

import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["number", "network"]), UniqueConstraint(columnNames = ["network", "mac"])])
data class Node(
    @Id
    @GeneratedValue
    val key: Long = 0,
    val number: Int = 0,
    val mac: String? = null,
    @JsonProperty("created_at")
    val createdAt: Long = 0,

    @JsonProperty("last_seen")
    val lastSeen: Long = 0,
    val location: String? = null,
    val network: String? = null,
)