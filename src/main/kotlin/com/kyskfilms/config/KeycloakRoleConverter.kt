package com.kyskfilms.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class KeycloakRoleConverter(
    private val clientId: String
) : Converter<Jwt, Collection<GrantedAuthority>> {

    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()

        // Извлекаем realm roles
        val realmRoles = extractRealmRoles(jwt)
        authorities.addAll(realmRoles.map { SimpleGrantedAuthority("ROLE_${it.uppercase()}") })

        // Извлекаем client roles
        val clientRoles = extractClientRoles(jwt, clientId)
        authorities.addAll(clientRoles.map { SimpleGrantedAuthority("ROLE_${it.uppercase()}") })

        return authorities
    }

    /**
     * Извлекает роли на уровне realm из токена
     */
    private fun extractRealmRoles(jwt: Jwt): Collection<String> {
        val realmAccess = jwt.getClaim<Map<String, Any>>("realm_access") ?: return emptyList()

        @Suppress("UNCHECKED_CAST")
        val roles = realmAccess["roles"] as? List<String> ?: return emptyList()

        return roles
    }

    /**
     * Извлекает роли на уровне клиента из токена
     */
    private fun extractClientRoles(jwt: Jwt, clientId: String): Collection<String> {
        val resourceAccess = jwt.getClaim<Map<String, Any>>("resource_access") ?: return emptyList()

        @Suppress("UNCHECKED_CAST")
        val clientAccess = resourceAccess[clientId] as? Map<String, Any> ?: return emptyList()

        @Suppress("UNCHECKED_CAST")
        val roles = clientAccess["roles"] as? List<String> ?: return emptyList()

        return roles
    }
}