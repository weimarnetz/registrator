package de.weimarnetz.registrator.services;

import org.springframework.stereotype.Component;

import de.weimarnetz.registrator.configuration.NetworksConfiguration;

import lombok.AllArgsConstructor;


@Component
@AllArgsConstructor
public class NetworkVerificationService {

    private final NetworksConfiguration networksConfiguration;

    public boolean isNetworkValid(String network) {
        return networksConfiguration.getMap().containsKey(network);
    }
}
