package com.pgsr.security

import com.pgsr.config.JwtProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
    private val props: JwtProperties
) {
    private val key: SecretKey by lazy {
        val secret = props.secret
        require(secret.isNotBlank()) { "JWT secret must be configured (security.jwt.secret)" }
        val bytes = try {
            // support base64 secrets, else use raw bytes
            Decoders.BASE64.decode(secret)
        } catch (e: IllegalArgumentException) {
            secret.toByteArray()
        }
        Keys.hmacShaKeyFor(bytes)
    }

    fun generateAccessToken(user: UserDetails): String {
        val now = Instant.now()
        val exp = now.plus(props.accessExpirationMinutes, ChronoUnit.MINUTES)
        return Jwts.builder()
            .subject(user.username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(user: UserDetails): Pair<String, Instant> {
        val now = Instant.now()
        val exp = now.plus(props.refreshExpirationDays, ChronoUnit.DAYS)
        val token = Jwts.builder()
            .subject(user.username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(key)
            .compact()
        return token to exp
    }

    fun extractUsername(token: String): String? =
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.subject
        } catch (e: Exception) {
            null
        }

    fun isTokenValid(token: String, user: UserDetails): Boolean {
        val username = extractUsername(token) ?: return false
        if (username != user.username) return false
        return try {
            val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            val exp = claims.payload.expiration.toInstant()
            exp.isAfter(Instant.now())
        } catch (e: Exception) {
            false
        }
    }
}
