package de.weimarnetz.registrator.services

import de.weimarnetz.registrator.configuration.NetworkInformation
import de.weimarnetz.registrator.configuration.NetworksConfiguration
import de.weimarnetz.registrator.exceptions.NetworkNotFoundException
import de.weimarnetz.registrator.exceptions.NoMoreNodesException
import de.weimarnetz.registrator.model.Node
import de.weimarnetz.registrator.repository.RegistratorRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.util.Maps
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NodeNumberServiceTest {
    private val registratorRepository: RegistratorRepository = mockk()

    private val networksConfiguration: NetworksConfiguration = mockk()

    private val nodeNumberService: NodeNumberService = NodeNumberService(registratorRepository, networksConfiguration)

    @BeforeEach
    fun setup() {
        val networkInformation = NetworkInformation(10, 2)
        val networkInformationMap = Maps.newHashMap("testnet", networkInformation)
        every { networksConfiguration.map } returns networkInformationMap
    }

    @Test
    fun nextNodeNumberOnEmptyList() {
        // given
        every { registratorRepository.findAllByNetwork("testnet") } returns emptyList()

        // when
        val nextAvailableNodeNumber = nodeNumberService.getNextAvailableNodeNumber("testnet")

        // then
        assertThat(nextAvailableNodeNumber).isEqualTo(2)
    }

    @Test
    fun nextNodeNumberOnFilledList() {
        // given
        every { registratorRepository.findAllByNetwork("testnet") } returns getNodeList(2, 3, 4, 6)

        // when
        val nextAvailableNodeNumber = nodeNumberService.getNextAvailableNodeNumber("testnet")

        // then
        assertThat(nextAvailableNodeNumber).isEqualTo(5)
    }

    @Test
    fun firstNodeNumberOnFilledList() {
        // given
        every { registratorRepository.findAllByNetwork("testnet") } returns getNodeList(3, 4, 6)

        // when
        val nextAvailableNodeNumber = nodeNumberService.getNextAvailableNodeNumber("testnet")

        // then
        assertThat(nextAvailableNodeNumber).isEqualTo(2)
    }

    @Test
    fun nextNodeNumberOnFilledListLeftHalf() {
        // given
        every { registratorRepository.findAllByNetwork("testnet") } returns getNodeList(2, 4, 5, 6, 7, 8, 9, 10)

        // when
        val nextAvailableNodeNumber = nodeNumberService.getNextAvailableNodeNumber("testnet")

        // then
        assertThat(nextAvailableNodeNumber).isEqualTo(3)
    }

    @Test
    fun nextNodeNumberOnFilledListMiddle() {
        // given
        every { registratorRepository.findAllByNetwork("testnet") } returns getNodeList(2, 3, 4, 5, 7, 8, 9, 10)

        // when
        val nextAvailableNodeNumber = nodeNumberService.getNextAvailableNodeNumber("testnet")

        // then
        assertThat(nextAvailableNodeNumber).isEqualTo(6)
    }

    @Test
    fun lastNodeNumberOnFilledList() {
        // given
        every { registratorRepository.findAllByNetwork("testnet") } returns getNodeList(2, 3, 4, 5, 8, 6, 7, 9)

        // when
        val nextAvailableNodeNumber = nodeNumberService.getNextAvailableNodeNumber("testnet")

        // then
        assertThat(nextAvailableNodeNumber).isEqualTo(10)
    }

    @Test
    fun noMoreNewNodesOnFilledList() {
        // given
        every { registratorRepository.findAllByNetwork("testnet") } returns getNodeList(2, 3, 4, 5, 8, 6, 7, 9, 10)

        // when
        org.junit.jupiter.api.Assertions.assertThrows(NoMoreNodesException::class.java) {
            nodeNumberService.getNextAvailableNodeNumber(
                "testnet"
            )
        }
    }

    @Test
    fun netWorkNotFound() {
        // given
        every { registratorRepository.findAllByNetwork("testnet") } returns getNodeList(2, 3, 4, 5, 8, 6, 7, 9, 10)

        // when
        assertThrows<NetworkNotFoundException> { nodeNumberService.getNextAvailableNodeNumber("not_my_network") }
    }

    private fun getNodeList(vararg nodeNumbers: Int): List<Node> = nodeNumbers.map { Node(number = it) }

    @Test
    fun nodeNumberToSmall() {
        // when
        val nodeNumberValid = nodeNumberService.isNodeNumberValid(1, "testnet")

        // then
        assertThat(nodeNumberValid).isFalse
    }

    @Test
    fun nodeNumberToBig() {
        // when
        val nodeNumberValid = nodeNumberService.isNodeNumberValid(11, "testnet")

        // then
        assertThat(nodeNumberValid).isFalse
    }

    @Test
    fun nodeNumberInRange() {
        // when
        val nodeNumberValid = nodeNumberService.isNodeNumberValid(5, "testnet")

        // then
        assertThat(nodeNumberValid).isTrue
    }
}