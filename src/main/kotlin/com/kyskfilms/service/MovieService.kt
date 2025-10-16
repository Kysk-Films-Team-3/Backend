package com.kyskfilms.service

import com.kyskfilms.dto.*
import com.kyskfilms.entity.Movie
import com.kyskfilms.repository.MovieRepository
import com.kyskfilms.repository.GenreRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class MovieService(
    private val movieRepository: MovieRepository,
    private val genreRepository: GenreRepository
) {

    fun getAllMovies(pageable: Pageable): Page<MovieCatalogDto> {
        return movieRepository.findAll(pageable).map { it.toCatalogDto() }
    }

    fun getMovieById(id: Long): MovieCatalogDto {
        val movie = movieRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found with id: $id")
        }
        return movie.toCatalogDto()
    }

    fun getMoviePlayerData(id: Long): MoviePlayerDto {
        val movie = movieRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found with id: $id")
        }
        return movie.toPlayerDto()
    }

    fun searchMovies(title: String, pageable: Pageable): Page<MovieCatalogDto> {
        return movieRepository.findByTitleContaining(title, pageable)
            .map { it.toCatalogDto() }
    }

    fun getMoviesByGenre(genreName: String, pageable: Pageable): Page<MovieCatalogDto> {
        return movieRepository.findByGenreName(genreName, pageable)
            .map { it.toCatalogDto() }
    }

    fun getTopRatedMovies(pageable: Pageable): Page<MovieCatalogDto> {
        return movieRepository.findAllByOrderByRatingDesc(pageable)
            .map { it.toCatalogDto() }
    }

    fun getLatestMovies(pageable: Pageable): Page<MovieCatalogDto> {
        return movieRepository.findAllByOrderByYearDesc(pageable)
            .map { it.toCatalogDto() }
    }

    fun createMovie(createMovieDto: CreateMovieDto): MovieCatalogDto {
        val genres = genreRepository.findAllById(createMovieDto.genreIds)
        val movie = Movie(
            titleUa = createMovieDto.titleUa,
            titleEn = createMovieDto.titleEn,
            description = createMovieDto.description,
            year = createMovieDto.year,
            duration = createMovieDto.duration,
            poster = createMovieDto.poster,
            videoHls = createMovieDto.videoHls,
            videoDash = createMovieDto.videoDash,
            genres = genres.toMutableSet(),
            actors = createMovieDto.actors.toMutableList()
        )
        val savedMovie = movieRepository.save(movie)
        return savedMovie.toCatalogDto()
    }

    fun updateMovie(id: Long, updateMovieDto: CreateMovieDto): MovieCatalogDto {
        val movie = movieRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found with id: $id")
        }

        val genres = genreRepository.findAllById(updateMovieDto.genreIds)
        val updatedMovie = movie.copy(
            titleUa = updateMovieDto.titleUa,
            titleEn = updateMovieDto.titleEn,
            description = updateMovieDto.description,
            year = updateMovieDto.year,
            duration = updateMovieDto.duration,
            poster = updateMovieDto.poster,
            videoHls = updateMovieDto.videoHls,
            videoDash = updateMovieDto.videoDash,
            genres = genres.toMutableSet(),
            actors = updateMovieDto.actors.toMutableList()
        )

        val savedMovie = movieRepository.save(updatedMovie)
        return savedMovie.toCatalogDto()
    }

    fun deleteMovie(id: Long) {
        if (!movieRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found with id: $id")
        }
        movieRepository.deleteById(id)
    }
}

// Extension functions for mapping
fun Movie.toCatalogDto() = MovieCatalogDto(
    id = this.id!!,
    titleUa = this.titleUa,
    titleEn = this.titleEn,
    poster = this.poster,
    year = this.year,
    genres = this.genres.map { it.name },
    rating = this.rating,
    duration = this.duration
)

fun Movie.toPlayerDto() = MoviePlayerDto(
    id = this.id!!,
    titleUa = this.titleUa,
    titleEn = this.titleEn,
    videoUrls = VideoUrls(
        hls = this.videoHls,
        dash = this.videoDash
    ),
    subtitles = this.subtitles?.map { SubtitleDto(it.language, it.url) } ?: emptyList(),
    description = this.description,
    actors = this.actors ?: emptyList(),
    genres = this.genres.map { it.name },
    year = this.year
)