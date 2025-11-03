package com.pgsr.controller

import com.pgsr.dto.MeResponse
import com.pgsr.security.CustomUserDetails
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class UserController {

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: CustomUserDetails): ResponseEntity<MeResponse> {
        val resp = MeResponse(
            id = principal.id(),
            email = principal.email(),
            role = principal.authorities.firstOrNull()?.authority ?: "ROLE_ADMIN"
        )
        return ResponseEntity.ok(resp)
    }
}
