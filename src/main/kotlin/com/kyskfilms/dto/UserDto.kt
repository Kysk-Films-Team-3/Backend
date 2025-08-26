package com.kyskfilms.dto

import com.kyskfilms.entity.SubscriptionType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank


// Profile DTOs
data class UserDto(
    val id: Long,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePicture: String? = null,
    val subscriptionType: SubscriptionType = SubscriptionType.BASIC
)

data class CreateUserDto(
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val keycloakId: String? = null,
    val subscriptionType: SubscriptionType = SubscriptionType.BASIC
)

data class UpdateUserDto(
    val firstName: String?,
    val lastName: String?,
    val profilePicture: String?,
    val subscriptionType: SubscriptionType? = null
)