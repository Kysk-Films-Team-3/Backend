package com.kyskfilms.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

class KeycloakRoleConverter(private val clientId: String) : Converter<Jwt, Collection<GrantedAuthority>> {

    private val defaultConverter = JwtGrantedAuthoritiesConverter()

    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()

        // Добавляем стандартные authorities (scope)
        defaultConverter.convert(jwt)?.let { authorities.addAll(it) }

        // Извлекаем роли из realm_access
        extractRealmRoles(jwt)?.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
        }

        // Извлекаем роли из resource_access для конкретного клиента
        extractResourceRoles(jwt)?.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
        }

        // Добавляем username как authority для удобства
        jwt.getClaimAsString("preferred_username")?.let {
            authorities.add(SimpleGrantedAuthority("USER_$it"))
        }

        return authorities
    }

    private fun extractRealmRoles(jwt: Jwt): List<String>? {
        val realmAccess = jwt.getClaim<Map<String, Any>>("realm_access") ?: return null

        @Suppress("UNCHECKED_CAST")
        return (realmAccess["roles"] as? List<String>)
    }

    private fun extractResourceRoles(jwt: Jwt): List<String>? {
        val resourceAccess = jwt.getClaim<Map<String, Any>>("resource_access") ?: return null

        // Используем переданный clientId или берем из токена
        val effectiveClientId = clientId.ifEmpty {
            jwt.getClaimAsString("azp") ?: return null
        }

        @Suppress("UNCHECKED_CAST")
        val clientAccess = resourceAccess[effectiveClientId] as? Map<String, Any> ?: return null

        @Suppress("UNCHECKED_CAST")
        return (clientAccess["roles"] as? List<String>)
    }
}