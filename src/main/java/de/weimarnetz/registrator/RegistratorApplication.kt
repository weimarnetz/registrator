package de.weimarnetz.registrator

import de.weimarnetz.registrator.configuration.NetworksConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(NetworksConfiguration::class)
class RegistratorApplication

fun main(args: Array<String>) {
    runApplication<RegistratorApplication>(*args)
}
