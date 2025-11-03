package com.pgsr

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class PgsrApplication

fun main(args: Array<String>) {
    runApplication<PgsrApplication>(*args)
}
