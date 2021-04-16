package de.weimarnetz.registrator.services;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import de.weimarnetz.registrator.configuration.NetworksConfiguration;


@Component
public class NetworkVerificationService {

    @Inject
    private NetworksConfiguration networksConfiguration;

    public boolean isNetworkValid(String network) {
        return networksConfiguration.getMap().containsKey(network);
    }
}
