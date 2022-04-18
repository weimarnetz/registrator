package de.weimarnetz.registrator.services

import de.weimarnetz.registrator.configuration.NetworksConfiguration
import de.weimarnetz.registrator.exceptions.NetworkNotFoundException
import de.weimarnetz.registrator.exceptions.NoMoreNodesException
import de.weimarnetz.registrator.repository.RegistratorRepository
import org.springframework.stereotype.Component

@Component
class NodeNumberService(
    private val registratorRepository: RegistratorRepository,
    private val networksConfiguration: NetworksConfiguration
) {
    @Throws(NoMoreNodesException::class, NetworkNotFoundException::class)
    fun getNextAvailableNodeNumber(network: String): Int {
        val nodeNumberBoundaries = getNodeNumberBoundaries(network)
        val nodeList = registratorRepository.findAllByNetwork(network)
        val nodeNumbersList = nodeList
            .map { it.number }
            .sorted()
            .toList()
        if (nodeNumbersList.size > nodeNumberBoundaries.second - nodeNumberBoundaries.first) {
            throw NoMoreNodesException()
        }
        return findFirstMissing(nodeNumbersList, 0, nodeNumbersList.size - 1, network)
    }

    @Throws(NetworkNotFoundException::class)
    fun isNodeNumberValid(nodeNumber: Int, network: String?): Boolean {
        return nodeNumber >= getNodeNumberBoundaries(network).first && nodeNumber <= getNodeNumberBoundaries(network).second
    }

    private fun findFirstMissing(list: List<Int>, start: Int, end: Int, network: String?): Int {
        val minNodeNumber: Int = getNodeNumberBoundaries(network).first
        val mid = (start + end) / 2
        // Left half has all elements from 0 to mid
        return when {
            list.isEmpty() -> minNodeNumber
            start > end -> end + 1 + minNodeNumber
            start + minNodeNumber != list[start] -> start + minNodeNumber
            list[mid] == mid + minNodeNumber -> findFirstMissing(list, mid + 1, end, network)

            // Left half has all elements from 0 to mid
            else -> findFirstMissing(list, start, mid, network)
        }

    }

    @Throws(NetworkNotFoundException::class)
    private fun getNodeNumberBoundaries(network: String?): Pair<Int, Int> {
        return if (networksConfiguration.map.containsKey(network)) {
            Pair(
                networksConfiguration.map[network]!!.minNodeNumber,
                networksConfiguration.map[network]!!.maxNodeNumber
            )
        } else {
            throw NetworkNotFoundException()
        }
    }
}