package de.weimarnetz.registrator.controller

import de.weimarnetz.registrator.exceptions.NoMoreNodesException
import de.weimarnetz.registrator.model.Node
import de.weimarnetz.registrator.model.NodeResponse
import de.weimarnetz.registrator.model.NodesResponse
import de.weimarnetz.registrator.repository.RegistratorRepository
import de.weimarnetz.registrator.services.LinkService
import de.weimarnetz.registrator.services.MacAddressService
import de.weimarnetz.registrator.services.NetworkVerificationService
import de.weimarnetz.registrator.services.NodeNumberService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.Date

@RestController
class RegistratorController(
    private val registratorRepository: RegistratorRepository,
    private val nodeNumberService: NodeNumberService,
    private val networkVerificationService: NetworkVerificationService,
    private val linkService: LinkService,
    private val macAddressService: MacAddressService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping(value = ["/time", "/GET/time"])
    @ResponseBody
    fun getTime(): ResponseEntity<Map<String, Long>> {
        val time = mapOf("now" to Date().time)
        return ResponseEntity.ok(time)
    }

    @GetMapping(value = ["/{network}/knoten/{nodeNumber}", "/GET/{network}/knoten/{nodeNumber}"])
    @ResponseBody
    fun getSingleNode(
        @PathVariable network: String,
        @PathVariable nodeNumber: Int
    ): ResponseEntity<NodeResponse> {
        if (networkVerificationService.isNetworkValid(network)) {
            val node = registratorRepository.findByNumberAndNetwork(nodeNumber, network)
            if (node != null) {
                return ResponseEntity.ok(NodeResponse(node = node, status = HttpStatus.OK.value(), message = "ok"))
            }
        } else {
            log.error { "Network $network not found!" }
        }
        return ResponseEntity.notFound().build()
    }

    @PostMapping(value = ["/{network}/knoten"])
    @ResponseBody
    fun registerNodePost(
        @PathVariable network: String,
        @RequestParam mac: String?
    ): ResponseEntity<NodeResponse?>? {
        if (!macAddressService.isValidMacAddress(mac)) {
            return ResponseEntity.badRequest().build()
        }
        if (!networkVerificationService.isNetworkValid(network)) {
            log.error { "Network $network not found! mac: $mac" }
            return ResponseEntity.notFound().build()
        }
        val normalizedMac = macAddressService.normalizeMacAddress(mac)
        val node = registratorRepository.findByNetworkAndMac(network, normalizedMac)
        val currentTime = System.currentTimeMillis()
        if (node != null) {
            val updatedNode = node.copy(lastSeen = currentTime)
            registratorRepository.save(updatedNode)
            val nodeResponse = NodeResponse(status = 303, message = "MAC already registered", node = updatedNode)
            return ResponseEntity.status(HttpStatus.SEE_OTHER).body(nodeResponse)
        }
        val newNodeNumber: Int = try {
            nodeNumberService.getNextAvailableNodeNumber(network)
        } catch (e: NoMoreNodesException) {
            log.error(e) { "No more node numbers available" }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
        return saveNewNode(network, normalizedMac, currentTime, newNodeNumber)
    }

    @GetMapping(value = ["/{network}/knoten", "/GET/{network}/knoten", "/{network}/list", "/GET/{network}/list"])
    @ResponseBody
    fun getNodes(@PathVariable network: String): ResponseEntity<NodesResponse?>? {
        if (!networkVerificationService.isNetworkValid(network)) {
            log.error { "Network $network not found!" }
            return ResponseEntity.notFound().build()
        }
        val nodesResponse =
            NodesResponse(
                node = registratorRepository.findAllByNetwork(network),
                message = "Ok",
                status = 200
            )
        return ResponseEntity.ok(nodesResponse)
    }

    @GetMapping(value = ["/POST/{network}/knoten"])
    @ResponseBody
    fun registerNodeGet(
        @PathVariable network: String,
        @RequestParam mac: String?
    ): ResponseEntity<NodeResponse?>? {
        return registerNodePost(network, mac)
    }

    @PutMapping(value = ["/{network}/knoten/{nodeNumber}"])
    @ResponseBody
    fun updateNodePut(
        @PathVariable network: String,
        @PathVariable nodeNumber: Int,
        @RequestParam mac: String?
    ): ResponseEntity<NodeResponse?>? {
        if (!macAddressService.isValidMacAddress(mac)) {
            return ResponseEntity.badRequest().build()
        }
        if (!networkVerificationService.isNetworkValid(network)) {
            log.warn { "Network $network not found!" }
            return ResponseEntity.notFound().build()
        }
        if (!nodeNumberService.isNodeNumberValid(nodeNumber, network)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
        val normalizedMac = macAddressService.normalizeMacAddress(mac)
        val nodeByNumber = registratorRepository.findByNumberAndNetwork(nodeNumber, network)
        val currentTime = System.currentTimeMillis()
        if (nodeByNumber == null && registratorRepository.findByNetworkAndMac(network, normalizedMac) == null) {
            return saveNewNode(network, normalizedMac, currentTime, nodeNumber)
        }
        if (nodeByNumber != null && normalizedMac == nodeByNumber.mac) {
            val updatedNode = nodeByNumber.copy(lastSeen = currentTime)
            registratorRepository.save(updatedNode)
            return ResponseEntity.ok(NodeResponse(node = updatedNode, status = 200, message = "updated"))
        }
        val nodeResponse =
            NodeResponse(message = "Unauthorized, wrong pass", status = 401, node = nodeByNumber)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(nodeResponse)
    }

    @GetMapping(value = ["/PUT/{network}/knoten/{nodeNumber}"])
    @ResponseBody
    fun updateNodeGet(
        @PathVariable network: String,
        @PathVariable nodeNumber: Int,
        @RequestParam mac: String?
    ): ResponseEntity<NodeResponse?>? {
        return updateNodePut(network, nodeNumber, mac)
    }

    @GetMapping(value = ["/{network}/knotenByMac"])
    @ResponseBody
    fun getNodeByMac(
        @PathVariable network: String,
        @RequestParam mac: String?
    ): ResponseEntity<NodeResponse?>? {
        if (!macAddressService.isValidMacAddress(mac)) {
            return ResponseEntity.badRequest().build()
        }
        if (networkVerificationService.isNetworkValid(network)) {
            val node = registratorRepository.findByNetworkAndMac(network, macAddressService.normalizeMacAddress(mac))
            if (node != null) {
                val nodeResponse = NodeResponse(node = node, status = HttpStatus.OK.value(), message = "ok")
                return ResponseEntity.ok(nodeResponse)
            }
        } else {
            log.error { "Network $network not found!" }
        }
        return ResponseEntity.notFound().build()
    }

    private fun saveNewNode(
        network: String,
        normalizedMac: String?,
        currentTime: Long,
        nodeNumber: Int
    ): ResponseEntity<NodeResponse?> {
        val newNode = Node(
            number = nodeNumber,
            createdAt = currentTime,
            lastSeen = currentTime,
            mac = normalizedMac,
            network = network,
            location = linkService.getNodeLocation(network, nodeNumber)
        )
        registratorRepository.save(newNode)
        val nodeResponse = NodeResponse(node = newNode, status = 201, message = "Node created!")
        return ResponseEntity.created(linkService.getNodeLocationUri(network, nodeNumber)).body(nodeResponse)
    }

    @DeleteMapping(value = ["/{network}/knoten/{nodeNumber}"])
    @ResponseBody
    fun deleteNodeNumber(
        @PathVariable network: String,
        @PathVariable nodeNumber: Int
    ): ResponseEntity<Any> {
        if (networkVerificationService.isNetworkValid(network)) {
            val node = registratorRepository.findByNumberAndNetwork(nodeNumber, network)
            if (node != null) {
                registratorRepository.delete(node)
                return ResponseEntity.noContent().build()
            }
        }
        return ResponseEntity.notFound().build()
    }

    @GetMapping("/dumpDatabase")
    @ResponseBody
    fun dumpDatabase(): ResponseEntity<List<Node>> {
        val nodes = registratorRepository.findAll().toList()
        return ResponseEntity.ok(nodes)
    }

    @PostMapping("/importDatabase")
    @ResponseBody
    fun importDatabase(@RequestBody nodes: List<Node>): ResponseEntity<Any> {
        val nodeList = nodes
            .filter { it.network != null }
            .map {
                val storedNode = registratorRepository.findByNumberAndNetwork(it.number, it.network!!)
                storedNode?.copy(number = it.number, network = it.network, mac = it.mac, createdAt = it.createdAt)
                    ?: it.copy(key = null)
            }
        registratorRepository.saveAll(nodeList)
        return ResponseEntity.ok().build()
    }

}