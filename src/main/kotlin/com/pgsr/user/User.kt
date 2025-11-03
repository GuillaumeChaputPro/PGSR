package com.pgsr.user

import jakarta.persistence.*

@Entity
@Table(name = "users", uniqueConstraints = [UniqueConstraint(columnNames = ["email"])])
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val email: String = "",

    @Column(nullable = false)
    var password: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.ADMIN,
)
