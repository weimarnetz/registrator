package de.weimarnetz.registrator.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import de.weimarnetz.registrator.configuration.NetworksConfiguration;
import de.weimarnetz.registrator.exceptions.ResourceNotFoundException;
import de.weimarnetz.registrator.model.Node;
import de.weimarnetz.registrator.repository.RegistratorRepository;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class WebController {

    private final RegistratorRepository registratorRepository;
    private final NetworksConfiguration networksConfiguration;

    @GetMapping("/{network:[a-zA-Z0-9\\-]*$}")
    public ModelAndView displayNodesByNetwork(
            @PathVariable String network
    ) {
        if (!networksConfiguration.getMap().containsKey(network)) {
            throw new ResourceNotFoundException("Network " + network + " not found!");
        }
        List<Node> allNodes = registratorRepository.findAllByNetworkOrderByLastSeenDesc(network);
        ModelAndView mav = new ModelAndView("allNodes");
        mav.addObject("nodeList", allNodes);
        mav.addObject("network", network);
        mav.addObject("networkConfig", networksConfiguration.getMap().get(network));
        mav.addObject("name", "Weimarnetz Registrator");
        return mav;
    }

    @GetMapping("/")
    public ModelAndView displayNodes() {
        return displayNodesByNetwork("ffweimar");
    }

}
