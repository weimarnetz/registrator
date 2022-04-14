package de.weimarnetz.registrator.services

import de.weimarnetz.registrator.controller.RegistratorController
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.stereotype.Component
import java.net.URI

@Component
class LinkService {
    fun getNodeLocation(network: String, nodeNumber: Int): String {
        return linkTo<RegistratorController> { getSingleNode(network, nodeNumber) }.toUri().path
    }

    fun getNodeLocationUri(network: String, nodeNumber: Int): URI {
        return linkTo<RegistratorController> { getSingleNode(network, nodeNumber) }.toUri()
    }
}