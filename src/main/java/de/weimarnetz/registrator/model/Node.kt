package de.weimarnetz.registrator.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.Version

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["number", "network"]), UniqueConstraint(columnNames = ["network", "mac"])])
data class Node(
    @Id
    @GeneratedValue
    val key: Long? = null,
    val number: Int = 0,
    val mac: String? = null,
    @JsonProperty("created_at")
    val createdAt: Long = 0,

    @JsonProperty("last_seen")
    val lastSeen: Long = 0,
    val location: String? = null,
    val network: String? = null,
    @Version
    @JsonIgnore
    val version: Long? = null
)