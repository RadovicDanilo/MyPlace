package com.radovicdanilo.pixelwar.config

import com.radovicdanilo.pixelwar.socket.PixelWebSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val pixelWebSocketHandler: PixelWebSocketHandler
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(pixelWebSocketHandler, "/ws")
            .setAllowedOrigins("*") // Allow all origins for now
    }
}