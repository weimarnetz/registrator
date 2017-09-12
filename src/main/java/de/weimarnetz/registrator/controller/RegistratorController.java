package de.weimarnetz.registrator.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import de.weimarnetz.registrator.model.Node;
import de.weimarnetz.registrator.model.NodeResponse;
import de.weimarnetz.registrator.model.NodesResponse;
import de.weimarnetz.registrator.repository.RegistratorRepository;

@RestController
@Slf4j
public class RegistratorController {

    @Inject
    private RegistratorRepository registratorRepository;

    @GetMapping(value = {"/time",
            "/GET/time"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    public @ResponseBody ResponseEntity<Map<String, Long>> getTime() {
        Map<String, Long> time = Collections.singletonMap("now", new Date().getTime());
        return ResponseEntity.ok(time);
    }

    @GetMapping(value= {"/{network}/knoten/{nodeNumber}",
        "/GET/{network}/knoten/{nodeNumber}"})
    public @ResponseBody ResponseEntity<NodeResponse> getSingleNode(
            @PathVariable String network,
            @PathVariable int nodeNumber) {
        log.warn(nodeNumber + " " + network);

        Node node = registratorRepository.findByNumberAndNetwork(nodeNumber, network);
        if (node != null) {
            NodeResponse nodeResponse = NodeResponse.builder().node(node).status(HttpStatus.OK.value()).message("ok").build();
            return ResponseEntity.ok(nodeResponse);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = {"/{network}/knoten", "/GET/{network}/knoten"})
    public @ResponseBody ResponseEntity<NodesResponse> getNodes(
            @PathVariable String network) {
        NodesResponse nodesResponse = NodesResponse.builder()
                .node(registratorRepository.findAllByNetwork(network))
                .message("Ok")
                .status(200)
                .build();
        return ResponseEntity.ok(nodesResponse);
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "Created!"),
            @ApiResponse(code = 303, message = "MAC already registered!"),
            @ApiResponse(code = 404, message = "Network not found")

    })
    @PostMapping(value = "/{network}/knoten")
    public @ResponseBody ResponseEntity<NodeResponse> registerNodePost(
            @PathVariable String network,
            @RequestParam String mac,
            @RequestParam String pass
    ) {
        Node node = registratorRepository.findByNetworkAndMac(network, mac);
        if (node != null && ! node.getPass().equals(pass)) {
            // use PUT method instead!
            NodeResponse nodeResponse = NodeResponse.builder()
                    .message("method not allowed")
                    .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                    .node(node)
                    .build();
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(nodeResponse);
        }
        return ResponseEntity.created(UriComponentsBuilder.fromHttpUrl("http://weimarnetz.de").build().toUri()).body(null);
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "Created!"),
            @ApiResponse(code = 303, message = "MAC already registered!"),
            @ApiResponse(code = 404, message = "Network not found")

    })
    @GetMapping(value = "/POST/{network}/knoten")
    public @ResponseBody ResponseEntity<NodeResponse> registerNodeGet(
            @PathVariable String network,
            @RequestParam String mac,
            @RequestParam String pass
    ) {
        return registerNodePost(network, mac, pass);
    }


    @ApiResponses({
            @ApiResponse(code = 200, message = "OK!"),
            @ApiResponse(code = 201, message = "Created!"),
            @ApiResponse(code = 401, message = "Wrong pass!!"),
            @ApiResponse(code = 404, message = "Network not found")

    })
    @PutMapping(value = "/{network}/knoten")
    public @ResponseBody ResponseEntity<NodeResponse> updateNodePut(
            @PathVariable String network,
            @RequestParam String mac,
            @RequestParam String pass
    ) {
        Node node = registratorRepository.findByNetworkAndMac(network, mac);
        long currentTime = new Date().getTime();
        if (pass.equals(node.getPass())) {
            node.setCreatedAt(currentTime);
            registratorRepository.save(node);
            return ResponseEntity.ok(null);
        } else {
        }
        return ResponseEntity.ok(null);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK!"),
            @ApiResponse(code = 201, message = "Created!"),
            @ApiResponse(code = 401, message = "Wrong pass!!"),
            @ApiResponse(code = 404, message = "Network not found")

    })
    @GetMapping(value = "/PUT/{network}/knoten")
    public @ResponseBody ResponseEntity<NodeResponse> updateNodeGet(
            @PathVariable String network,
            @RequestParam String mac,
            @RequestParam String pass
    ) {
        return updateNodePut(network, mac, pass);
    }
}
