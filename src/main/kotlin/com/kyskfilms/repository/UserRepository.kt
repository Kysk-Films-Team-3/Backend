package com.kyskfilms.repository

import com.kyskfilms.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByKeycloakId(keycloakId: String): User?
    fun existsByEmail(email: String): Boolean
}