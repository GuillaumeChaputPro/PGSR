package com.pgsr.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// Requests

data class RegisterRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank @field:Size(min = 8, max = 100) val password: String
)

data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String
)

data class RefreshTokenRequest(
    @field:NotBlank val refreshToken: String
)

// Responses

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)

data class MeResponse(
    val id: Long,
    val email: String,
    val role: String
)

data class ApiError(
    val status: Int,
    val error: String,
    val message: String?,
    val path: String?
)
