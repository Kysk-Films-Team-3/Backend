package com.kyskfilms.repository

import com.kyskfilms.entity.Movie
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MovieRepository : JpaRepository<Movie, Long> {

    @Query("SELECT m FROM Movie m WHERE LOWER(m.titleUa) LIKE LOWER(CONCAT('%', :title, '%')) OR LOWER(m.titleEn) LIKE LOWER(CONCAT('%', :title, '%'))")
    fun findByTitleContaining(@Param("title") title: String, pageable: Pageable): Page<Movie>

    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE LOWER(g.name) = LOWER(:genreName)")
    fun findByGenreName(@Param("genreName") genreName: String, pageable: Pageable): Page<Movie>

    fun findAllByOrderByRatingDesc(pageable: Pageable): Page<Movie>

    fun findAllByOrderByYearDesc(pageable: Pageable): Page<Movie>

}