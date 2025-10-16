package com.kyskfilms.service

import com.kyskfilms.dto.UserDto
import com.kyskfilms.entity.User
import com.kyskfilms.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Сервис для синхронизации пользователей между Keycloak и локальной БД
 *
 * При первом запросе от пользователя:
 * 1. Извлекает данные из JWT токена
 * 2. Проверяет существование пользователя в БД по keycloakId
 * 3. Если нет - создает нового пользователя
 * 4. Если есть - обновляет данные (email, имя) если изменились
 */
@Service
class KeycloakUserService(
    private val userRepository: UserRepository,
    private val orElse: OrElse
) {
    private val logger = LoggerFactory.getLogger(KeycloakUserService::class.java)

    /**
     * Получает или создает пользователя на основе JWT токена
     *
     * @param jwt JWT токен от Keycloak
     * @return UserDto с данными пользователя
     */
    @Transactional
    fun getOrCreateUser(jwt: Jwt): UserDto {
        val keycloakId = jwt.subject
        val email = jwt.getClaim<String>("email") ?: throw IllegalArgumentException("Email not found in JWT")
        val preferredUsername = jwt.getClaim<String>("preferred_username")
        val givenName = jwt.getClaim<String>("given_name")
        val familyName = jwt.getClaim<String>("family_name")

        logger.debug("Processing user from JWT - keycloakId: $keycloakId, email: $email")

        // Ищем пользователя по keycloakId
        var user = userRepository.findByKeycloakId(keycloakId)

        if (user == null) {
            // Проверяем по email (на случай если пользователь был создан до интеграции с Keycloak)
            user = userRepository.findByEmail(email)

            if (user != null) {
                // Обновляем keycloakId для существующего пользователя
                logger.info("Linking existing user (email: $email) with Keycloak ID: $keycloakId")
                user.keycloakId = keycloakId
            } else {
                // Создаем нового пользователя
                logger.info("Creating new user from Keycloak - email: $email, keycloakId: $keycloakId")
                user = User(
                    email = email,
                    keycloakId = keycloakId,
                    firstName = givenName,
                    lastName = familyName
                )
            }
        } else {
            // Обновляем данные пользователя если изменились
            var updated = false

            if (user.email != email) {
                logger.info("Updating email for user $keycloakId: ${user.email} -> $email")
                user.email = email
                updated = true
            }

            if (user.firstName != givenName) {
                user.firstName = givenName
                updated = true
            }

            if (user.lastName != familyName) {
                user.lastName = familyName
                updated = true
            }

            if (updated) {
                logger.debug("User data updated for keycloakId: $keycloakId")
            }
        }

        // Сохраняем пользователя
        user = userRepository.save(user)

        return UserDto(
            id = user.id!!,
            email = user.email,
            keycloakId = user.keycloakId,
            firstName = user.firstName,
            lastName = user.lastName,
            profilePicture = user.profilePicture,
            subscriptionType = user.subscriptionType.name,
            createdAt = user.createdAt
        )
    }

    private fun UserDto(
        id: Long,
        email: String,
        keycloakId: String?,
        firstName: String?,
        lastName: String?,
        profilePicture: String?,
        subscriptionType: String,
        createdAt: LocalDateTime
    ): UserDto {
        TODO("Not yet implemented")
    }

    /**
     * Получает ID пользователя по JWT токену (создает если не существует)
     */
    @Transactional
    fun getUserIdFromJwt(jwt: Jwt): Long {
        return getOrCreateUser(jwt).id
    }

    /**
     * Извлекает роли пользователя из JWT токена
     */
    fun extractRoles(jwt: Jwt): List<String> {
        val roles = mutableListOf<String>()

        // Realm roles
        val realmAccess = jwt.getClaim<Map<String, Any>>("realm_access")
        if (realmAccess != null) {
            @Suppress("UNCHECKED_CAST")
            val realmRoles = realmAccess["roles"] as? List<String>
            if (realmRoles != null) {
                roles.addAll(realmRoles)
            }
        }

        // Client roles
        val resourceAccess = jwt.getClaim<Map<String, Any>>("resource_access")
        if (resourceAccess != null) {
            resourceAccess.values.forEach { clientAccess ->
                @Suppress("UNCHECKED_CAST")
                val clientRoles = (clientAccess as? Map<String, Any>)?.get("roles") as? List<String>
                if (clientRoles != null) {
                    roles.addAll(clientRoles)
                }
            }
        }

        return roles
    }

    /**
     * Проверяет есть ли у пользователя определенная роль
     */
    fun hasRole(jwt: Jwt, role: String): Boolean {
        return extractRoles(jwt).any { it.equals(role, ignoreCase = true) }
    }
}

