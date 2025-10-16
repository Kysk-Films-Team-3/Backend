package com.kyskfilms.service

import com.kyskfilms.dto.CreateUserDto
import com.kyskfilms.dto.UserDto
import com.kyskfilms.entity.User
import com.kyskfilms.entity.SubscriptionType
import com.kyskfilms.repository.UserRepository
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class KeycloakUserService(
    private val userRepository: UserRepository
) {

    fun getOrCreateUser(jwt: Jwt): UserDto {
        val keycloakId = jwt.getClaimAsString("sub")
        val email = jwt.getClaimAsString("email")
        val firstName = jwt.getClaimAsString("given_name")
        val lastName = jwt.getClaimAsString("family_name")

        val existingUser = userRepository.findByKeycloakId(keycloakId)

        return if (existingUser != null) {
            existingUser.toDto()
        } else {
            val newUser = User(
                email = email,
                keycloakId = keycloakId,
                firstName = firstName,
                lastName = lastName
            )
            userRepository.save(newUser).toDto()
        }
    }

    private fun User.toDto() = UserDto(
        id = id ?: 0L, // Исправляем проблему с Long? -> Long
        email = email,
        firstName = firstName,
        lastName = lastName,
        profilePicture = profilePicture,
        subscriptionType = subscriptionType
    )
}