package com.kyskfilms.dto

import com.kyskfilms.entity.SubscriptionType
import java.time.LocalDateTime


// Profile DTOs

data class CreateUserDto(
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val keycloakId: String? = null,
    val subscriptionType: SubscriptionType = SubscriptionType.BASIC
)


// ============ UserDto ============
data class UserDto(
    val id: Long,
    val email: String,
    val keycloakId: String?,
    val firstName: String?,
    val lastName: String?,
    val profilePicture: String?,
    val subscriptionType: SubscriptionType,
    val createdAt: LocalDateTime
)

// ============ UserProfileDto (для /api/auth/me) ============
data class UserProfileDto(
    val id: Long,
    val email: String,
    val name: String?,
    val role: String,
    val avatar: String?
)

// ============ UpdateUserDto ============
abstract class UpdateUserDto(
    val firstName: String?,
    val lastName: String?,
    val profilePicture: String?
) {
    abstract val subscriptionType: Any
}