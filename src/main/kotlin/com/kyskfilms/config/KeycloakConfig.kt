package com.kyskfilms.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KeycloakProperties::class)
class KeycloakConfig

@ConfigurationProperties(prefix = "keycloak")
data class KeycloakProperties(
    val realm: String = "kyskfilms",
    val authServerUrl: String = "http://localhost:8080",
    val clientId: String = "kyskfilms-backend",
    val bearerOnly: Boolean = true,
    val cors: Boolean = true,
    val corsMaxAge: Int = 1000,
    val corsAllowedMethods: String = "POST, PUT, DELETE, GET, OPTIONS, PATCH",
    val corsAllowedHeaders: String = "*"
)