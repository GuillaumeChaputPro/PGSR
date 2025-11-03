package com.pgsr.security

import com.pgsr.user.User
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "refresh_tokens", indexes = [Index(columnList = "token", unique = true)])
data class RefreshToken(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true, length = 512)
    val token: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @Column(nullable = false)
    val expiryAt: Instant = Instant.now(),

    @Column(nullable = false)
    var revoked: Boolean = false,
)
