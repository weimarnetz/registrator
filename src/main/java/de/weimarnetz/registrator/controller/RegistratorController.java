package de.weimarnetz.registrator.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.weimarnetz.registrator.exceptions.NoMoreNodesException;
import de.weimarnetz.registrator.model.Node;
import de.weimarnetz.registrator.model.NodeResponse;
import de.weimarnetz.registrator.model.NodesResponse;
import de.weimarnetz.registrator.repository.RegistratorRepository;
import de.weimarnetz.registrator.services.LinkService;
import de.weimarnetz.registrator.services.MacAddressService;
import de.weimarnetz.registrator.services.NetworkVerificationService;
import de.weimarnetz.registrator.services.NodeNumberService;
import de.weimarnetz.registrator.services.PasswordService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@AllArgsConstructor
public class RegistratorController {

    private static final String NETWORK_NOT_FOUND = "Network {} not found!";
    private final RegistratorRepository registratorRepository;
    private final NodeNumberService nodeNumberService;
    private final NetworkVerificationService networkVerificationService;
    private final PasswordService passwordService;
    private final LinkService linkService;
    private final MacAddressService macAddressService;

    @GetMapping(value = { "/time",
            "/GET/time" })
    public @ResponseBody
    ResponseEntity<Map<String, Long>> getTime() {
        Map<String, Long> time = Collections.singletonMap("now", new Date().getTime());
        return ResponseEntity.ok(time);
    }

    @GetMapping(value = { "/{network}/knoten/{nodeNumber}", "/GET/{network}/knoten/{nodeNumber}" })
    public @ResponseBody
    ResponseEntity<NodeResponse> getSingleNode(
            @PathVariable String network,
            @PathVariable int nodeNumber) {

        if (networkVerificationService.isNetworkValid(network)) {
            Node node = registratorRepository.findByNumberAndNetwork(nodeNumber, network);
            if (node != null) {
                NodeResponse nodeResponse = NodeResponse.builder().node(node).status(HttpStatus.OK.value()).message("ok").build();
                return ResponseEntity.ok(nodeResponse);
            }
        } else {
            log.error(NETWORK_NOT_FOUND, network);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/{network}/knoten")
    public @ResponseBody ResponseEntity<NodeResponse> registerNodePost(
            @PathVariable String network,
            @RequestParam String mac,
            @RequestParam String pass
    ) {
        if (!macAddressService.isValidMacAddress(mac)) {
            return ResponseEntity.badRequest().build();
        }
        if (!networkVerificationService.isNetworkValid(network)) {
            log.error(" {}, network: {}, mac: {}", NETWORK_NOT_FOUND, network, mac);
            return ResponseEntity.notFound().build();
        }

        String normalizedMac = macAddressService.normalizeMacAddress(mac);
        Node node = registratorRepository.findByNetworkAndMac(network, normalizedMac);
        long currentTime = System.currentTimeMillis();
        if (node != null) {
            node.setLastSeen(currentTime);
            registratorRepository.save(node);
            NodeResponse nodeResponse = NodeResponse.builder().status(303).message("MAC already registered").node(node).build();
            return ResponseEntity.status(HttpStatus.SEE_OTHER).body(nodeResponse);
        }
        int newNodeNumber;
        try {
            newNodeNumber = nodeNumberService.getNextAvailableNodeNumber(network);
        } catch (NoMoreNodesException e) {
            log.error("No more node numbers available", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return saveNewNode(network, normalizedMac, pass, currentTime, newNodeNumber);
    }

    @GetMapping(value = { "/{network}/knoten", "/GET/{network}/knoten", "/{network}/list", "/GET/{network}/list" })
    public @ResponseBody
    ResponseEntity<NodesResponse> getNodes(@PathVariable String network) {
        if (!networkVerificationService.isNetworkValid(network)) {
            log.error(NETWORK_NOT_FOUND, network);
            return ResponseEntity.notFound().build();
        }
        NodesResponse nodesResponse = NodesResponse.builder().node(registratorRepository.findAllByNetwork(network)).message("Ok").status(200).build();
        return ResponseEntity.ok(nodesResponse);
    }

    @GetMapping(value = "/POST/{network}/knoten")
    public @ResponseBody ResponseEntity<NodeResponse> registerNodeGet(
            @PathVariable String network,
            @RequestParam String mac,
            @RequestParam String pass
    ) {
        return registerNodePost(network, mac, pass);
    }


    @PutMapping(value = "/{network}/knoten/{nodeNumber}")
    public @ResponseBody ResponseEntity<NodeResponse> updateNodePut(
            @PathVariable String network,
            @PathVariable int nodeNumber,
            @RequestParam String mac,
            @RequestParam String pass
    ) {
        if (!macAddressService.isValidMacAddress(mac)) {
            return ResponseEntity.badRequest().build();
        }
        if (!networkVerificationService.isNetworkValid(network)) {
            log.warn("{}, network: {}, mac: {}, node: {}", NETWORK_NOT_FOUND, network, mac, nodeNumber);
            return ResponseEntity.notFound().build();
        }

        if (!nodeNumberService.isNodeNumberValid(nodeNumber, network)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        String normalizedMac = macAddressService.normalizeMacAddress(mac);
        Node nodeByNumber = registratorRepository.findByNumberAndNetwork(nodeNumber, network);
        long currentTime = System.currentTimeMillis();
        if (nodeByNumber == null && registratorRepository.findByNetworkAndMac(network, normalizedMac) == null) {
            return saveNewNode(network, normalizedMac, pass, currentTime, nodeNumber);
        }
        if (nodeByNumber != null && normalizedMac.equals(nodeByNumber.getMac())) {
            nodeByNumber.setLastSeen(currentTime);
            registratorRepository.save(nodeByNumber);
            NodeResponse nodeResponse = NodeResponse.builder().node(nodeByNumber).status(200).message("updated").build();
            return ResponseEntity.ok(nodeResponse);
        }
        NodeResponse nodeResponse = NodeResponse.builder().message("Unauthorized, wrong pass").status(401).node(nodeByNumber).build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(nodeResponse);
    }

    @GetMapping(value = "/PUT/{network}/knoten/{nodeNumber}")
    public @ResponseBody ResponseEntity<NodeResponse> updateNodeGet(
            @PathVariable String network,
            @PathVariable int nodeNumber,
            @RequestParam String mac,
            @RequestParam String pass
    ) {
        return updateNodePut(network, nodeNumber, mac, pass);
    }

    @PutMapping(value = "/{network}/updatepassword/{nodeNumber}")
    public @ResponseBody
    ResponseEntity<NodeResponse> updatePasswordPut(
            @PathVariable String network,
            @PathVariable int nodeNumber,
            @RequestParam String mac,
            @RequestParam String oldPass,
            @RequestParam String newPass
    ) {
        if (!macAddressService.isValidMacAddress(mac)) {
            return ResponseEntity.badRequest().build();
        }
        if (networkVerificationService.isNetworkValid(network)) {
            Node node = registratorRepository.findByNumberAndNetwork(nodeNumber, network);
            if (node != null && macAddressService.normalizeMacAddress(mac).equals(node.getMac())) {
                node.setPass(passwordService.encryptPassword(newPass));
                registratorRepository.save(node);
                NodeResponse nodeResponse = NodeResponse.builder().node(node).status(200).message("password changed").build();
                return ResponseEntity.ok(nodeResponse);
            } else if (node != null) {
                NodeResponse nodeResponse = NodeResponse.builder().message("Unauthorized, wrong pass or mac").status(401).node(node).build();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(nodeResponse);
            }
        } else {
            log.warn(NETWORK_NOT_FOUND, network);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/{network}/knotenByMac")
    public @ResponseBody
    ResponseEntity<NodeResponse> getNodeByMac(
            @PathVariable String network,
            @RequestParam String mac
    ) {
        if (!macAddressService.isValidMacAddress(mac)) {
            return ResponseEntity.badRequest().build();
        }

        if (networkVerificationService.isNetworkValid(network)) {
            Node node = registratorRepository.findByNetworkAndMac(network, macAddressService.normalizeMacAddress(mac));
            if (node != null) {
                NodeResponse nodeResponse = NodeResponse.builder().node(node).status(HttpStatus.OK.value()).message("ok").build();
                return ResponseEntity.ok(nodeResponse);
            }
        } else {
            log.error(NETWORK_NOT_FOUND, network);
        }
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<NodeResponse> saveNewNode(String network, String normalizedMac, String pass, long currentTime, int nodeNumber) {
        Node newNode = Node.builder().number(nodeNumber).createdAt(currentTime).lastSeen(currentTime).mac(normalizedMac).pass(passwordService.encryptPassword(pass)).network(network).location(linkService.getNodeLocation(network, nodeNumber)).build();
        registratorRepository.save(newNode);
        NodeResponse nodeResponse = NodeResponse.builder().node(newNode).status(201).message("Node created!").build();
        return ResponseEntity.created(linkService.getNodeLocationUri(network, nodeNumber)).body(nodeResponse);
    }

    @DeleteMapping(value = "/{network}/knoten/{nodeNumber}")
    public @ResponseBody
    ResponseEntity<Object> deleteNodeNumber(
            @PathVariable String network,
            @PathVariable int nodeNumber
    ) {
        if (networkVerificationService.isNetworkValid(network)) {
            Node node = registratorRepository.findByNumberAndNetwork(nodeNumber, network);
            if (node != null) {
                registratorRepository.delete(node);
                return ResponseEntity.noContent().build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/dumpDatabase")
    public @ResponseBody ResponseEntity<NodesResponse> dumpDatabase() {
        ArrayList<Node> nodeArrayList = new ArrayList<>();
        Iterable<Node> nodes = registratorRepository.findAll();
        nodes.iterator().forEachRemaining(nodeArrayList::add);
        NodesResponse.NodesResponseBuilder nodesResponse = NodesResponse.builder()
                .message("all nodes collected")
                .status(200)
                .node(nodeArrayList);
        return ResponseEntity.ok(nodesResponse.build());
    }
}
