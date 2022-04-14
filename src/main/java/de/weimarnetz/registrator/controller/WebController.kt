package de.weimarnetz.registrator.controller

import de.weimarnetz.registrator.configuration.NetworksConfiguration
import de.weimarnetz.registrator.exceptions.ResourceNotFoundException
import de.weimarnetz.registrator.repository.RegistratorRepository
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView

@Controller
class WebController(
    private val registratorRepository: RegistratorRepository,
    private val networksConfiguration: NetworksConfiguration
) {
    @GetMapping("/{network:[a-zA-Z0-9\\-]*$}")
    fun displayNodesByNetwork(
        @PathVariable network: String
    ): ModelAndView {
        if (!networksConfiguration.map.containsKey(network)) {
            throw ResourceNotFoundException("Network $network not found!")
        }
        val allNodes = registratorRepository.findAllByNetworkOrderByLastSeenDesc(network)
        val mav = ModelAndView("allNodes")
        mav.addObject("nodeList", allNodes)
        mav.addObject("network", network)
        mav.addObject("networkConfig", networksConfiguration.map[network])
        mav.addObject("name", "Weimarnetz Registrator")
        return mav
    }

    @GetMapping("/")
    fun displayNodes(): ModelAndView {
        return displayNodesByNetwork("ffweimar")
    }
}