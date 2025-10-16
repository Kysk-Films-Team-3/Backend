package com.kyskfilms.controller

import com.kyskfilms.dto.*
import com.kyskfilms.service.KeycloakUserService
import com.kyskfilms.service.UserService
import com.kyskfilms.service.WatchlistService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
class UserController(
    private val userService: UserService,
    private val watchlistService: WatchlistService,
    private val keycloakUserService: KeycloakUserService
) {

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile")
    fun getCurrentUserProfile(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<ApiResponse<UserDto>> {
        val user = keycloakUserService.getOrCreateUser(jwt)
        return ResponseEntity.ok(
            ApiResponse(
                data = user,
                meta = Meta(message = "Profile retrieved successfully")
            )
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#id).email == authentication.name")
    @Operation(summary = "Get user by ID")
    fun getUserById(@PathVariable id: Long): ResponseEntity<ApiResponse<UserDto>> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(
            ApiResponse(
                data = user,
                meta = Meta(message = "User retrieved successfully")
            )
        )
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user profile")
    fun updateCurrentUserProfile(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody updateUserDto: UpdateUserDto
    ): ResponseEntity<ApiResponse<UserDto>> {
        val currentUser = keycloakUserService.getOrCreateUser(jwt)
        val user = userService.updateUser(currentUser.id, updateUserDto)
        return ResponseEntity.ok(
            ApiResponse(
                data = user,
                meta = Meta(message = "Profile updated successfully")
            )
        )
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#id).email == authentication.name")
    @Operation(summary = "Update user")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody updateUserDto: UpdateUserDto
    ): ResponseEntity<ApiResponse<UserDto>> {
        val user = userService.updateUser(id, updateUserDto)
        return ResponseEntity.ok(
            ApiResponse(
                data = user,
                meta = Meta(message = "User updated successfully")
            )
        )
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<ApiResponse<Nothing>> {
        userService.deleteUser(id)
        return ResponseEntity.ok(
            ApiResponse(
                meta = Meta(message = "User deleted successfully")
            )
        )
    }

    @GetMapping("/profile/watchlist")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's watchlist")
    fun getCurrentUserWatchlist(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<ApiResponse<List<MovieCatalogDto>>> {
        val currentUser = keycloakUserService.getOrCreateUser(jwt)
        val watchlist = watchlistService.getUserWatchlist(currentUser.id)
        return ResponseEntity.ok(
            ApiResponse(
                data = watchlist,
                meta = Meta(message = "Watchlist retrieved successfully")
            )
        )
    }

    @GetMapping("/{id}/watchlist")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#id).email == authentication.name")
    @Operation(summary = "Get user's watchlist")
    fun getUserWatchlist(@PathVariable id: Long): ResponseEntity<ApiResponse<List<MovieCatalogDto>>> {
        val watchlist = watchlistService.getUserWatchlist(id)
        return ResponseEntity.ok(
            ApiResponse(
                data = watchlist,
                meta = Meta(message = "Watchlist retrieved successfully")
            )
        )
    }

    @PostMapping("/profile/watchlist/{movieId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add movie to current user's watchlist")
    fun addToCurrentUserWatchlist(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable movieId: Long
    ): ResponseEntity<ApiResponse<MovieCatalogDto>> {
        val currentUser = keycloakUserService.getOrCreateUser(jwt)
        val movie = watchlistService.addToWatchlist(currentUser.id, movieId)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                data = movie,
                meta = Meta(message = "Movie added to watchlist")
            )
        )
    }

    @PostMapping("/{id}/watchlist/{movieId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#id).email == authentication.name")
    @Operation(summary = "Add movie to watchlist")
    fun addToWatchlist(
        @PathVariable id: Long,
        @PathVariable movieId: Long
    ): ResponseEntity<ApiResponse<MovieCatalogDto>> {
        val movie = watchlistService.addToWatchlist(id, movieId)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                data = movie,
                meta = Meta(message = "Movie added to watchlist")
            )
        )
    }

    @DeleteMapping("/profile/watchlist/{movieId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove movie from current user's watchlist")
    fun removeFromCurrentUserWatchlist(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable movieId: Long
    ): ResponseEntity<ApiResponse<Nothing>> {
        val currentUser = keycloakUserService.getOrCreateUser(jwt)
        watchlistService.removeFromWatchlist(currentUser.id, movieId)
        return ResponseEntity.ok(
            ApiResponse(
                meta = Meta(message = "Movie removed from watchlist")
            )
        )
    }

    @DeleteMapping("/{id}/watchlist/{movieId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#id).email == authentication.name")
    @Operation(summary = "Remove movie from watchlist")
    fun removeFromWatchlist(
        @PathVariable id: Long,
        @PathVariable movieId: Long
    ): ResponseEntity<ApiResponse<Nothing>> {
        watchlistService.removeFromWatchlist(id, movieId)
        return ResponseEntity.ok(
            ApiResponse(
                meta = Meta(message = "Movie removed from watchlist")
            )
        )
    }
}