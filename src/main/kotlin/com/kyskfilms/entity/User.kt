package com.kyskfilms.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    var email: String = "",

    @Column(name = "keycloak_id", unique = true)
    var keycloakId: String? = null,

    @Column(name = "first_name")
    var firstName: String? = null,

    @Column(name = "last_name")
    var lastName: String? = null,

    @Column(name = "profile_picture")
    var profilePicture: String? = null,

    @Column(name = "subscription_type")
    @Enumerated(EnumType.STRING)
    var subscriptionType: SubscriptionType = SubscriptionType.BASIC,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var watchlist: MutableList<Watchlist> = mutableListOf(),

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    // Конструктор без параметров для JPA
    constructor() : this(
        id = null,
        email = "",
        keycloakId = null,
        firstName = null,
        lastName = null,
        profilePicture = null,
        subscriptionType = SubscriptionType.BASIC,
        watchlist = mutableListOf(),
        createdAt = LocalDateTime.now()
    )

    // Удобный конструктор для создания пользователей
    constructor(
        email: String,
        keycloakId: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        subscriptionType: SubscriptionType = SubscriptionType.BASIC
    ) : this(
        id = null,
        email = email,
        keycloakId = keycloakId,
        firstName = firstName,
        lastName = lastName,
        profilePicture = null,
        subscriptionType = subscriptionType,
        watchlist = mutableListOf(),
        createdAt = LocalDateTime.now()
    )

    override fun toString(): String {
        return "User(id=$id, email='$email', firstName=$firstName, lastName=$lastName, subscriptionType=$subscriptionType)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

enum class SubscriptionType {
    BASIC, PREMIUM, ENTERPRISE
}