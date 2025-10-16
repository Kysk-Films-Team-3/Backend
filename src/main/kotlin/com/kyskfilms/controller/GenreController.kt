package com.kyskfilms.controller

import com.kyskfilms.dto.ApiResponse
import com.kyskfilms.dto.Meta
import com.kyskfilms.entity.Genre
import com.kyskfilms.repository.GenreRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/genres")
@Tag(name = "Genres", description = "Genre management endpoints")
class GenreController(private val genreRepository: GenreRepository) {

    @GetMapping
    @Operation(summary = "Get all genres")
    fun getAllGenres(): ResponseEntity<ApiResponse<List<Genre>>> {
        val genres = genreRepository.findAll()
        return ResponseEntity.ok(
            ApiResponse(
                data = genres,
                meta = Meta(message = "Genres retrieved successfully")
            )
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get genre by ID")
    fun getGenreById(@PathVariable id: Long): ResponseEntity<ApiResponse<Genre>> {
        val genre = genreRepository.findById(id).orElse(null)
        return if (genre != null) {
            ResponseEntity.ok(
                ApiResponse(
                    data = genre,
                    meta = Meta(message = "Genre retrieved successfully")
                )
            )
        } else {
            ResponseEntity.notFound().build()
        }
    }
}