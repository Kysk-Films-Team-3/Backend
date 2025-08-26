package com.kyskfilms.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "movies")
data class Movie(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "title_ua", nullable = false)
    val titleUa: String,

    @Column(name = "title_en", nullable = false)
    val titleEn: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(nullable = false)
    val year: Int,

    val duration: Int? = null,

    val poster: String? = null,

    @Column(name = "video_hls")
    val videoHls: String? = null,

    @Column(name = "video_dash")
    val videoDash: String? = null,

    val rating: Double? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "movie_genres",
        joinColumns = [JoinColumn(name = "movie_id")],
        inverseJoinColumns = [JoinColumn(name = "genre_id")]
    )
    val genres: MutableSet<Genre> = mutableSetOf(),

    @ElementCollection
    @CollectionTable(name = "movie_actors", joinColumns = [JoinColumn(name = "movie_id")])
    @Column(name = "actor")
    val actors: MutableList<String>? = null,

    @OneToMany(mappedBy = "movie", cascade = [CascadeType.ALL], orphanRemoval = true)
    val subtitles: MutableList<Subtitle>? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "subtitles")
data class Subtitle(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val language: String,
    val url: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    val movie: Movie
)

@Entity
@Table(name = "genres")
data class Genre(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val name: String
)