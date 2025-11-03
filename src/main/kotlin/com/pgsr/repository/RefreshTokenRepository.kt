package com.pgsr.repository

import com.pgsr.security.RefreshToken
import com.pgsr.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.*

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): Optional<RefreshToken>
    fun deleteByUser(user: User)
    fun deleteAllByExpiryAtBefore(cutoff: Instant): Long
}
