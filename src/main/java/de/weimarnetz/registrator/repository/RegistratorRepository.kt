package de.weimarnetz.registrator.repository

import de.weimarnetz.registrator.model.Node
import org.springframework.data.repository.CrudRepository

interface RegistratorRepository : CrudRepository<Node, Long> {
    fun findByNumberAndNetwork(number: Int, network: String): Node?
    fun findByNetworkAndMac(network: String, mac: String): Node?
    fun findAllByNetwork(network: String): List<Node>
    fun findAllByNetworkOrderByLastSeenDesc(network: String): List<Node>
}