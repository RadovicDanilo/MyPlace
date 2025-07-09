package com.radovicdanilo.pixelwar.security

import com.radovicdanilo.pixelwar.security.service.TokenService
import io.jsonwebtoken.Claims
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Aspect
@Component
class SecurityAspect(
    private val tokenService: TokenService, private val request: HttpServletRequest
) {

    @Around("@annotation(com.radovicdanilo.pixelwar.security.CheckSecurity)")
    @Throws(Throwable::class)
    fun around(joinPoint: ProceedingJoinPoint): Any? {
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method

        val authorizationHeader =
            request.getHeader("Authorization") ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build<Any>()

        if (!authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build<Any>()
        }

        val token = authorizationHeader.substring(7)

        val claims: Claims = try {
            tokenService.parseToken(token)
        } catch (_: Exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build<Any>()
        }

        val checkSecurity = method.getAnnotation(CheckSecurity::class.java)
        val role: String? = claims["role"] as? String

        if (checkSecurity.roles.isEmpty() || role in checkSecurity.roles.map { it.name }) {
            return joinPoint.proceed()
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build<Any>()
    }
}
