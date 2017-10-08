package de.weimarnetz.registrator.services;

import org.springframework.stereotype.Component;

import javax.inject.Inject;

import de.weimarnetz.registrator.configuration.NetworksConfiguration;


@Component
public class NetworkVerificationService {

    @Inject
    private NetworksConfiguration networksConfiguration;

    public boolean isNetworkValid(String network) {
        return networksConfiguration.getMap().containsKey(network);
    }
}
