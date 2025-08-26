package com.kyskfilms.repository

import com.kyskfilms.entity.Watchlist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface WatchlistRepository : JpaRepository<Watchlist, Long> {
    fun findByUserId(userId: Long): List<Watchlist>
    fun findByUserIdAndMovieId(userId: Long, movieId: Long): Watchlist?
    fun existsByUserIdAndMovieId(userId: Long, movieId: Long): Boolean

    @Modifying
    @Query("DELETE FROM Watchlist w WHERE w.user.id = :userId AND w.movie.id = :movieId")
    fun deleteByUserIdAndMovieId(userId: Long, movieId: Long)
}