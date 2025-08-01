package com.radovicdanilo.myplace.config

import com.radovicdanilo.myplace.config.security.AuthHandshakeInterceptor
import com.radovicdanilo.myplace.socket.PixelWebSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val pixelWebSocketHandler: PixelWebSocketHandler, private val handshakeInterceptor: AuthHandshakeInterceptor
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(pixelWebSocketHandler, "/ws").addInterceptors(
            handshakeInterceptor
        ).setAllowedOrigins("*")
    }
}
