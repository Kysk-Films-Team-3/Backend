package com.kyskfilms.controller

import com.kyskfilms.dto.*
import com.kyskfilms.service.MovieService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/movies")
@Tag(name = "Movies", description = "Movie management endpoints")
class MovieController(private val movieService: MovieService) {

    @GetMapping
    @Operation(summary = "Get all movies")
    fun getAllMovies(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
        @RequestParam(defaultValue = "titleUa") sortBy: String,
        @RequestParam(defaultValue = "asc") sortDir: String
    ): ResponseEntity<ApiResponse<List<MovieCatalogDto>>> {
        val sort = if (sortDir.equals("desc", ignoreCase = true))
            Sort.by(sortBy).descending()
        else
            Sort.by(sortBy).ascending()

        val pageable: Pageable = PageRequest.of(page - 1, pageSize, sort)
        val moviesPage = movieService.getAllMovies(pageable)

        return ResponseEntity.ok(
            ApiResponse(
                data = moviesPage.content,
                meta = Meta(
                    page = page,
                    pageSize = pageSize,
                    totalPages = moviesPage.totalPages,
                    totalElements = moviesPage.totalElements,
                    message = "Movies retrieved successfully"
                )
            )
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movie by ID for catalog")
    fun getMovieById(@PathVariable id: Long): ResponseEntity<ApiResponse<MovieCatalogDto>> {
        val movie = movieService.getMovieById(id)
        return ResponseEntity.ok(
            ApiResponse(
                data = movie,
                meta = Meta(message = "Movie retrieved successfully")
            )
        )
    }

    @GetMapping("/{id}/player")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get movie data for player")
    fun getMovieForPlayer(@PathVariable id: Long): ResponseEntity<ApiResponse<MoviePlayerDto>> {
        val movie = movieService.getMoviePlayerData(id)
        return ResponseEntity.ok(
            ApiResponse(
                data = movie,
                meta = Meta(message = "Movie data for player retrieved successfully")
            )
        )
    }

    @GetMapping("/search")
    @Operation(summary = "Search movies by title")
    fun searchMovies(
        @RequestParam title: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int
    ): ResponseEntity<ApiResponse<List<MovieCatalogDto>>> {
        val pageable = PageRequest.of(page - 1, pageSize)
        val moviesPage = movieService.searchMovies(title, pageable)

        return ResponseEntity.ok(
            ApiResponse(
                data = moviesPage.content,
                meta = Meta(
                    page = page,
                    pageSize = pageSize,
                    totalPages = moviesPage.totalPages,
                    totalElements = moviesPage.totalElements,
                    message = "Movies found"
                )
            )
        )
    }

    @GetMapping("/genre/{genreName}")
    @Operation(summary = "Get movies by genre")
    fun getMoviesByGenre(
        @PathVariable genreName: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int
    ): ResponseEntity<ApiResponse<List<MovieCatalogDto>>> {
        val pageable = PageRequest.of(page - 1, pageSize)
        val moviesPage = movieService.getMoviesByGenre(genreName, pageable)

        return ResponseEntity.ok(
            ApiResponse(
                data = moviesPage.content,
                meta = Meta(
                    page = page,
                    pageSize = pageSize,
                    totalPages = moviesPage.totalPages,
                    totalElements = moviesPage.totalElements,
                    message = "Movies retrieved by genre"
                )
            )
        )
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated movies")
    fun getTopRatedMovies(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int
    ): ResponseEntity<ApiResponse<List<MovieCatalogDto>>> {
        val pageable = PageRequest.of(page - 1, pageSize)
        val moviesPage = movieService.getTopRatedMovies(pageable)

        return ResponseEntity.ok(
            ApiResponse(
                data = moviesPage.content,
                meta = Meta(
                    page = page,
                    pageSize = pageSize,
                    totalPages = moviesPage.totalPages,
                    totalElements = moviesPage.totalElements,
                    message = "Top rated movies retrieved"
                )
            )
        )
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest movies")
    fun getLatestMovies(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int
    ): ResponseEntity<ApiResponse<List<MovieCatalogDto>>> {
        val pageable = PageRequest.of(page - 1, pageSize)
        val moviesPage = movieService.getLatestMovies(pageable)

        return ResponseEntity.ok(
            ApiResponse(
                data = moviesPage.content,
                meta = Meta(
                    page = page,
                    pageSize = pageSize,
                    totalPages = moviesPage.totalPages,
                    totalElements = moviesPage.totalElements,
                    message = "Latest movies retrieved"
                )
            )
        )
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new movie")
    fun createMovie(@Valid @RequestBody createMovieDto: CreateMovieDto): ResponseEntity<ApiResponse<MovieCatalogDto>> {
        val movie = movieService.createMovie(createMovieDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                data = movie,
                meta = Meta(message = "Movie created successfully")
            )
        )
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a movie")
    fun updateMovie(
        @PathVariable id: Long,
        @Valid @RequestBody updateMovieDto: CreateMovieDto
    ): ResponseEntity<ApiResponse<MovieCatalogDto>> {
        val movie = movieService.updateMovie(id, updateMovieDto)
        return ResponseEntity.ok(
            ApiResponse(
                data = movie,
                meta = Meta(message = "Movie updated successfully")
            )
        )
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a movie")
    fun deleteMovie(@PathVariable id: Long): ResponseEntity<ApiResponse<Nothing>> {
        movieService.deleteMovie(id)
        return ResponseEntity.ok(
            ApiResponse(
                meta = Meta(message = "Movie deleted successfully")
            )
        )
    }
}