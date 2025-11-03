package com.pgsr.config

import com.pgsr.dto.ApiError
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ApiError> {
        val msg = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return error(HttpStatus.BAD_REQUEST, msg, request)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraint(ex: ConstraintViolationException, request: HttpServletRequest): ResponseEntity<ApiError> =
        error(HttpStatus.BAD_REQUEST, ex.message, request)

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuth(ex: AuthenticationException, request: HttpServletRequest): ResponseEntity<ApiError> =
        error(HttpStatus.UNAUTHORIZED, ex.message, request)

    @ExceptionHandler(AccessDeniedException::class)
    fun handleDenied(ex: AccessDeniedException, request: HttpServletRequest): ResponseEntity<ApiError> =
        error(HttpStatus.FORBIDDEN, ex.message, request)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArg(ex: IllegalArgumentException, request: HttpServletRequest): ResponseEntity<ApiError> =
        error(HttpStatus.BAD_REQUEST, ex.message, request)

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception, request: HttpServletRequest): ResponseEntity<ApiError> =
        error(HttpStatus.INTERNAL_SERVER_ERROR, ex.message, request)

    private fun error(status: HttpStatus, message: String?, request: HttpServletRequest): ResponseEntity<ApiError> {
        val body = ApiError(status.value(), status.reasonPhrase, message, request.requestURI)
        return ResponseEntity.status(status).body(body)
    }
}
