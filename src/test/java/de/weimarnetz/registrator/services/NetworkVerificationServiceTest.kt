package de.weimarnetz.registrator.services

import de.weimarnetz.registrator.configuration.NetworkInformation
import de.weimarnetz.registrator.configuration.NetworksConfiguration
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NetworkVerificationServiceTest {
    private val networksConfiguration: NetworksConfiguration = mockk()

    private val networkVerificationService = NetworkVerificationService(networksConfiguration)

    @BeforeEach
    fun setup() {
        val networkInformationMap = mapOf("testnet" to NetworkInformation())
        every { networksConfiguration.map } returns networkInformationMap
    }

    @Test
    fun invalidNetworkTest() {
        // when
        val networkValid = networkVerificationService.isNetworkValid("not_our_netz")

        // then
        assertThat(networkValid).isFalse
    }

    @Test
    fun validNetworkTest() {
        // when
        val networkValid = networkVerificationService.isNetworkValid("testnet")

        // then
        assertThat(networkValid).isTrue
    }
}