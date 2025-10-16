package com.kyskfilms.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KeycloakProperties::class)
class KeycloakConfig

@ConfigurationProperties(prefix = "keycloak")
data class KeycloakProperties(
    var realm: String = "kyskfilms",
    var authServerUrl: String = "http://localhost:8080",
    var clientId: String = "kyskfilms-backend",
    var bearerOnly: Boolean = true,
    var cors: Boolean = true,
    var corsMaxAge: Int = 1000,
    var corsAllowedMethods: String = "POST,PUT,DELETE,GET,OPTIONS,PATCH",
    var corsAllowedHeaders: String = "*"
){
    fun getIssuerUri(): String = "$authServerUrl/realms/$realm"
    fun getJwkSetUri(): String = "$authServerUrl/realms/$realm/protocol/openid-connect/certs"
    fun getUserInfoUri(): String = "$authServerUrl/realms/$realm/protocol/openid-connect/userinfo"
}