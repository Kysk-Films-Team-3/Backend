package com.kyskfilms.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.kyskfilms.entity.SubscriptionType
import jakarta.validation.constraints.*


data class MovieDto(
    val id: Long?,
    val title: String,
    val description: String?,
    val releaseYear: Int?,
    val durationMinutes: Int?,
    val posterUrl: String?,
    val trailerUrl: String?,
    val videoUrl: String?,
    val rating: String,
    val imdbRating: Double?,
    val genres: List<String>
)





// Auth DTOs
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserProfileDto
)

data class UserProfileDto(
    val id: Long,
    val email: String,
    val name: String?,
    val role: String,
    val avatar: String? = null
)

// Movie DTOs for catalog
data class MovieCatalogDto(
    val id: Long,
    val titleUa: String,
    val titleEn: String,
    val poster: String?,
    val year: Int,
    val genres: List<String>,
    val rating: Double?,
    val duration: Int?
)

// Movie DTOs for player
data class MoviePlayerDto(
    val id: Long,
    val titleUa: String,
    val titleEn: String,
    val videoUrls: VideoUrls,
    val subtitles: List<SubtitleDto>,
    val description: String?,
    val actors: List<String>,
    val genres: List<String>,
    val year: Int
)

data class VideoUrls(
    val hls: String?,
    val dash: String?
)

data class SubtitleDto(
    val language: String,
    val url: String
)



data class CreateMovieDto(
    val titleUa: String,
    val titleEn: String,
    val description: String?,
    val year: Int,
    val duration: Int?,
    val poster: String?,
    val videoHls: String?,
    val videoDash: String?,
    val genreIds: List<Long>,
    val actors: List<String> = emptyList(),
    val subtitles: List<SubtitleDto> = emptyList()
)
