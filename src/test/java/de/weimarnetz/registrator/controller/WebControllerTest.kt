package de.weimarnetz.registrator.controller

import de.weimarnetz.registrator.configuration.NetworkInformation
import de.weimarnetz.registrator.configuration.NetworksConfiguration
import de.weimarnetz.registrator.exceptions.ResourceNotFoundException
import de.weimarnetz.registrator.repository.RegistratorRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private const val TESTNET = "testnet"
private const val FFWEIMAR = "ffweimar"

internal class WebControllerTest {
    private val registratorRepository: RegistratorRepository = mockk()

    private val networksConfiguration: NetworksConfiguration = mockk()

    private val webController: WebController = WebController(registratorRepository, networksConfiguration)

    @BeforeEach
    fun setUp() {
        val map = mapOf(FFWEIMAR to NetworkInformation(), TESTNET to NetworkInformation())
        every { networksConfiguration.map } returns map
        every { registratorRepository.findAllByNetworkOrderByLastSeenDesc(TESTNET) } returns emptyList()
        every { registratorRepository.findAllByNetworkOrderByLastSeenDesc(FFWEIMAR) } returns emptyList()
    }

    @Test
    fun displayNodesByNetwork() {
        // given
        every { registratorRepository.findAllByNetwork(TESTNET) } returns emptyList()
        // when
        val modelAndView = webController.displayNodesByNetwork(TESTNET)

        // then
        assertThat(modelAndView.viewName).isEqualTo("allNodes")
        assertThat(modelAndView.model).containsEntry("network", TESTNET)
    }

    @Test
    fun displayNodes() {
        // given
        every { registratorRepository.findAllByNetwork(FFWEIMAR) } returns emptyList()
        // when
        val modelAndView = webController.displayNodes()

        // then
        assertThat(modelAndView.viewName).isEqualTo("allNodes")
        assertThat(modelAndView.model).containsEntry("network", FFWEIMAR)
    }

    @Test
    fun networkNotFound() {
        // when
        assertThrows<ResourceNotFoundException> { webController.displayNodesByNetwork("nonexistingnetwork") }
    }

}