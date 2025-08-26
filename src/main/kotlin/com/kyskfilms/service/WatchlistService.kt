package com.kyskfilms.service

import com.kyskfilms.dto.MovieCatalogDto
import com.kyskfilms.exception.ResourceNotFoundException
import com.kyskfilms.repository.MovieRepository
import com.kyskfilms.repository.UserRepository
import com.kyskfilms.repository.WatchlistRepository
import com.kyskfilms.entity.Watchlist
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class WatchlistService(
    private val watchlistRepository: WatchlistRepository,
    private val userRepository: UserRepository,
    private val movieRepository: MovieRepository,
    private val movieService: MovieService
) {

    @Transactional(readOnly = true)
    fun getUserWatchlist(userId: Long): List<MovieCatalogDto> {
        if (!userRepository.existsById(userId)) {
            throw ResourceNotFoundException("User with id $userId not found")
        }

        return watchlistRepository.findByUserId(userId)
            .map { movieService.getMovieById(it.movie.id!!) }
    }

    fun addToWatchlist(userId: Long, movieId: Long): MovieCatalogDto {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User with id $userId not found") }

        val movie = movieRepository.findById(movieId)
            .orElseThrow { ResourceNotFoundException("Movie with id $movieId not found") }

        if (watchlistRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw IllegalArgumentException("Movie is already in watchlist")
        }

        val watchlist = Watchlist(user = user, movie = movie)
        watchlistRepository.save(watchlist)

        return movieService.getMovieById(movieId)
    }

    fun removeFromWatchlist(userId: Long, movieId: Long) {
        if (!userRepository.existsById(userId)) {
            throw ResourceNotFoundException("User with id $userId not found")
        }

        if (!movieRepository.existsById(movieId)) {
            throw ResourceNotFoundException("Movie with id $movieId not found")
        }

        if (!watchlistRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw ResourceNotFoundException("Movie is not in user's watchlist")
        }

        watchlistRepository.deleteByUserIdAndMovieId(userId, movieId)
    }
}