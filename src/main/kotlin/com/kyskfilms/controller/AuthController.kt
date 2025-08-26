package com.kyskfilms.controller

import com.kyskfilms.dto.*
import com.kyskfilms.service.KeycloakUserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
class AuthController(
    private val keycloakUserService: KeycloakUserService
) {

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user info")
    fun getCurrentUser(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<ApiResponse<UserProfileDto>> {
        val user = keycloakUserService.getOrCreateUser(jwt)
        val userProfile = UserProfileDto(
            id = user.id!!,
            email = user.email,
            name = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim().ifBlank { null },
            role = extractRole(jwt),
            avatar = user.profilePicture
        )

        return ResponseEntity.ok(
            ApiResponse(
                data = userProfile,
                meta = Meta(message = "User info retrieved successfully")
            )
        )
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout")
    fun logout(): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.ok(
            ApiResponse(
                meta = Meta(message = "Logout successful")
            )
        )
    }

    private fun extractRole(jwt: Jwt): String {
        val realmAccess = jwt.getClaim<Map<String, Any>>("realm_access")
        val roles = realmAccess?.get("roles") as? List<*>
        return when {
            roles?.contains("admin") == true -> "ADMIN"
            roles?.contains("premium") == true -> "PREMIUM"
            else -> "USER"
        }
    }
}