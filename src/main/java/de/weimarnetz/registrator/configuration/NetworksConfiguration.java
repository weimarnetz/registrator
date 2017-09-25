package de.weimarnetz.registrator.configuration;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "network")
@Component
@Data
public class NetworksConfiguration {

    private Map<String, NetworkInformation> map;
}
