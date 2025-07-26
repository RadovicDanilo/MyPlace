package com.radovicdanilo.pixelwar.socket

import com.fasterxml.jackson.databind.ObjectMapper
import com.radovicdanilo.pixelwar.service.CanvasService
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class PixelWebSocketHandler(
    private val objectMapper: ObjectMapper, private val canvasService: CanvasService
) : TextWebSocketHandler() {

    private val sessions = mutableSetOf<WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions += session
        println("WebSocket connected: ${session.id}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions -= session
        println("WebSocket disconnected: ${session.id}")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val payload = objectMapper.readTree(message.payload)
            val x = payload["x"].asInt()
            val y = payload["y"].asInt()
            val color = payload["color"].asInt()

            if (x > 1023 || y > 1023 || color > 15 || x < 0 || y < 0 || color < 0) {
                throw IllegalArgumentException("Invalid value")
            }

            canvasService.setPixel(x, y, color)

            val update = objectMapper.writeValueAsString(
                mapOf("type" to "pixel", "x" to x, "y" to y, "color" to color)
            )

            val msg = TextMessage(update)

            sessions.forEach { it.sendMessage(msg) }

        } catch (_: Exception) {
            println("Invalid WebSocket message: ${message.payload}")
        }
    }
}
