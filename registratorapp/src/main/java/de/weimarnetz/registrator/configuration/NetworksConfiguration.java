package de.weimarnetz.registrator.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties(prefix = "network")
@Component
@Data
public class NetworksConfiguration {

    private Map<String, NetworkInformation> map;
}
