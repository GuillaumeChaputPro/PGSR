package com.pgsr.controller

import com.pgsr.auth.AuthService
import com.pgsr.dto.AuthResponse
import com.pgsr.dto.LoginRequest
import com.pgsr.dto.RefreshTokenRequest
import com.pgsr.dto.RegisterRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody body: RegisterRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.register(body))

    @PostMapping("/login")
    fun login(@Valid @RequestBody body: LoginRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.login(body))

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody body: RefreshTokenRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.refresh(body))

    @PostMapping("/logout")
    fun logout(@Valid @RequestBody body: RefreshTokenRequest): ResponseEntity<Void> {
        authService.logout(body)
        return ResponseEntity.noContent().build()
    }
}
