package de.weimarnetz.registrator.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class NetworkVerificationService {

    @Value("#{'${networks}'.split(',')}")
    private List<String> validNetworks;

    public boolean isNetworkValid(String network) {
        return validNetworks.contains(network);
    }
}
