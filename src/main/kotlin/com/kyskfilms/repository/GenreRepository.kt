package com.kyskfilms.repository

import com.kyskfilms.entity.Genre
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GenreRepository : JpaRepository<Genre, Long> {
    fun findByName(name: String): Genre?
    fun findByNameIgnoreCase(name: String): Genre?
}