package de.weimarnetz.registrator.controller;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import de.weimarnetz.registrator.configuration.NetworksConfiguration;
import de.weimarnetz.registrator.exceptions.ResourceNotFoundException;
import de.weimarnetz.registrator.model.Node;
import de.weimarnetz.registrator.repository.RegistratorRepository;

@Controller
public class WebController {

    @Inject
    private RegistratorRepository registratorRepository;
    @Inject
    private NetworksConfiguration networksConfiguration;

    @GetMapping("/{network:[a-zA-Z0-9\\-]*$}")
    public ModelAndView displayNodesByNetwork(
            @PathVariable String network
    ) {
        if (!networksConfiguration.getMap().containsKey(network)) {
            throw new ResourceNotFoundException("Network " + network + " not found!");
        }
        List<Node> allNodes = registratorRepository.findAllByNetwork(network);
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
