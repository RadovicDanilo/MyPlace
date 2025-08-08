package com.radovicdanilo.myplace.config.sockets

import com.radovicdanilo.myplace.config.security.service.TokenService
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

    // Handles websocket authorization
    // We are using protocols to "smuggle" in the jwt token since Authorization headers aren't allowed in ws
    // set the userId attribute which is necessary foo managing cooldowns
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val protocols = request.headers["Sec-WebSocket-Protocol"]?.firstOrNull()
        val token = protocols?.split(", ")?.getOrNull(1)

        if (token == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
            return false
        }

        try {
            val userId = tokenService.getId(token)
            attributes.put("userId", userId)

            response.headers["Sec-WebSocket-Protocol"] = protocols.split(", ").first()
            return true
        } catch (_: Exception) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
            return false
        }
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: java.lang.Exception?
    ) {
    }

}