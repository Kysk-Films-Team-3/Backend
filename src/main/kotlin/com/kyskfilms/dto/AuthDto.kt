package com.kyskfilms.dto

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val data: T? = null,
    val meta: Meta? = null,
    val error: ErrorInfo? = null
) {
    val success: Boolean get() = error == null
    val message: String? get() = error?.message

    companion object {
        fun <T> success(data: T? = null, message: String? = null, meta: Meta? = null): ApiResponse<T> {
            return ApiResponse(data = data, meta = meta)
        }

        fun <T> error(message: String, error: ErrorInfo? = null): ApiResponse<T> {
            return ApiResponse(error = error ?: ErrorInfo("ERROR", message))
        }
    }
}

data class Meta(
    val page: Int? = null,
    val pageSize: Int? = null,
    val totalPages: Int? = null,
    val totalElements: Long? = null,
    val message: String? = null,
    val timestamp: String? = null
)

data class ErrorInfo(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)

data class LoginDto(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String
)

data class JwtResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserDto
)

