package com.pgsr.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.jwt")
data class JwtProperties(
    val secret: String = "",
    val accessExpirationMinutes: Long = 15,
    val refreshExpirationDays: Long = 7,
)
