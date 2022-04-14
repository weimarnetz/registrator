package de.weimarnetz.registrator.services

import de.weimarnetz.registrator.configuration.NetworksConfiguration
import org.springframework.stereotype.Component

@Component
class NetworkVerificationService(private val networksConfiguration: NetworksConfiguration) {
    fun isNetworkValid(network: String?): Boolean {
        return networksConfiguration.map.containsKey(network)
    }
}