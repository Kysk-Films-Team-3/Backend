package com.kyskfilms.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "watchlist",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "movie_id"])]
)
data class Watchlist(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    val movie: Movie,

    @Column(name = "added_at")
    val addedAt: LocalDateTime = LocalDateTime.now()
)