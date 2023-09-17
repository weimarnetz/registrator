package de.weimarnetz.registrator.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "network")
data class NetworksConfiguration(
    val map: Map<String, NetworkInformation>
)

data class NetworkInformation(
    val maxNodeNumber: Int = 0,
    val minNodeNumber: Int = 0,
    val leaseDays: Int = 0
)