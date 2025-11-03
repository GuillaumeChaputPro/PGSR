package com.pgsr.auth

import com.pgsr.dto.AuthResponse
import com.pgsr.dto.LoginRequest
import com.pgsr.dto.RefreshTokenRequest
import com.pgsr.dto.RegisterRequest
import com.pgsr.repository.RefreshTokenRepository
import com.pgsr.repository.UserRepository
import com.pgsr.security.CustomUserDetails
import com.pgsr.security.JwtService
import com.pgsr.security.RefreshToken
import com.pgsr.user.Role
import com.pgsr.user.User
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        require(!userRepository.existsByEmail(request.email)) { "Email already registered" }

        val user = userRepository.save(
            User(
                email = request.email.lowercase(),
                password = passwordEncoder.encode(request.password),
                role = Role.ADMIN,
            )
        )

        val userDetails = CustomUserDetails(user)
        return issueTokens(userDetails)
    }

    fun login(request: LoginRequest): AuthResponse {
        val authToken = UsernamePasswordAuthenticationToken(request.email, request.password)
        authenticationManager.authenticate(authToken)
        val user = userRepository.findByEmail(request.email).orElseThrow()
        val userDetails = CustomUserDetails(user)
        return issueTokens(userDetails)
    }

    @Transactional
    fun refresh(request: RefreshTokenRequest): AuthResponse {
        val rt = refreshTokenRepository.findByToken(request.refreshToken)
            .orElseThrow { IllegalArgumentException("Invalid refresh token") }
        if (rt.revoked) throw IllegalArgumentException("Refresh token revoked")
        if (rt.expiryAt.isBefore(Instant.now())) throw IllegalArgumentException("Refresh token expired")

        val user = rt.user
        // rotate refresh token: revoke old and issue new
        rt.revoked = true
        refreshTokenRepository.save(rt)
        val userDetails = CustomUserDetails(user)
        return issueTokens(userDetails)
    }

    @Transactional
    fun logout(request: RefreshTokenRequest) {
        val rt = refreshTokenRepository.findByToken(request.refreshToken)
            .orElseThrow { IllegalArgumentException("Invalid refresh token") }
        rt.revoked = true
        refreshTokenRepository.save(rt)
    }

    private fun issueTokens(user: UserDetails): AuthResponse {
        val access = jwtService.generateAccessToken(user)
        val (refresh, exp) = jwtService.generateRefreshToken(user)
        // persist refresh token
        val entity = RefreshToken(
            token = refresh,
            user = (user as? CustomUserDetails)?.let { itUser ->
                userRepository.findByEmail(itUser.username).orElseThrow()
            } ?: throw IllegalStateException("Unsupported principal type"),
            expiryAt = exp,
        )
        refreshTokenRepository.save(entity)
        return AuthResponse(access, refresh)
    }
}
