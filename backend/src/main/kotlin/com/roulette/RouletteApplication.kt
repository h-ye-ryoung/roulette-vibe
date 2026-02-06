package com.roulette

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class RouletteApplication

fun main(args: Array<String>) {
    runApplication<RouletteApplication>(*args)
}
