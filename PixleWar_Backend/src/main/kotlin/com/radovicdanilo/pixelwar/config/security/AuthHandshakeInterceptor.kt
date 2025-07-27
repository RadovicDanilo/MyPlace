package com.radovicdanilo.pixelwar.config.security

import com.radovicdanilo.pixelwar.config.security.service.TokenService
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class AuthHandshakeInterceptor(
    private val tokenService: TokenService
) : HandshakeInterceptor {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val token = request.headers.getFirst("Authorization")?.substringAfter("Bearer ")
        if (token == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
            return false
        }
        try {
            attributes["userId"] = tokenService.getId(token)
            return true
        } catch (e: Exception) {
            println(e.message)
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED)
        return false
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: java.lang.Exception?
    ) {
    }

}
