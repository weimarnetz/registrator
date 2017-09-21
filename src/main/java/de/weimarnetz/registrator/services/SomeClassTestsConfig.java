package de.weimarnetz.registrator.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Properties;

@Configuration
public class SomeClassTestsConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() throws Exception {
        final PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        Properties properties = new Properties();

        properties.setProperty("nodenumber.min", "2");

        pspc.setProperties(properties);
        return pspc;
    }


}