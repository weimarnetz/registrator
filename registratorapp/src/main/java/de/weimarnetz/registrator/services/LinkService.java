package de.weimarnetz.registrator.services;

import org.springframework.stereotype.Component;

import java.net.URI;

import de.weimarnetz.registrator.controller.RegistratorController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class LinkService {

    public String getNodeLocation(String network, int nodeNumber) {
        return linkTo(methodOn(RegistratorController.class).getSingleNode(network, nodeNumber)).toUri().getPath();
    }

    public URI getNodeLocationUri(String network, int nodeNumber) {
        return linkTo(methodOn(RegistratorController.class).getSingleNode(network, nodeNumber)).toUri();
    }
}
