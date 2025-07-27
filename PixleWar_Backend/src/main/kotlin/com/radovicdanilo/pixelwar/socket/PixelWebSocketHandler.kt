package com.radovicdanilo.pixelwar.socket

import com.fasterxml.jackson.databind.ObjectMapper
import com.radovicdanilo.pixelwar.constants.CanvasConstants.CANVAS_HEIGHT
import com.radovicdanilo.pixelwar.constants.CanvasConstants.CANVAS_WIDTH
import com.radovicdanilo.pixelwar.constants.CanvasConstants.MAX_COLOR
import com.radovicdanilo.pixelwar.service.CanvasService
import com.radovicdanilo.pixelwar.service.UserCooldownService
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

@Component
class PixelWebSocketHandler(
    private val objectMapper: ObjectMapper,
    private val canvasService: CanvasService,
    private val userCooldownService: UserCooldownService
) : TextWebSocketHandler() {

    private val activeSessions = ConcurrentHashMap.newKeySet<WebSocketSession>()
    private val lock = ReentrantLock()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        activeSessions.add(session)
        println("WebSocket connected: ${session.id}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        activeSessions.remove(session)
        println("WebSocket disconnected: ${session.id}")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val payload = objectMapper.readTree(message.payload)
            val x = payload["x"].asInt()
            val y = payload["y"].asInt()
            val color = payload["color"].asInt()
            val userId = getAuthenticatedUserId(session)

            require(x in 0 until CANVAS_WIDTH) { "x coordinate out of bounds" }
            require(y in 0 until CANVAS_HEIGHT) { "y coordinate out of bounds" }
            require(color in 0..MAX_COLOR) { "color must be 4-bit (0-15)" }

            if (!lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                session.sendMessage(TextMessage("""{"error":"Server busy, try again"}"""))
                return
            }
            try {
                if (!userCooldownService.canPlacePixel(userId)) {
                    session.sendMessage(TextMessage("""{"error":"Cooldown active"}"""))
                    return
                }

                canvasService.setPixel(x, y, color)
                userCooldownService.updateLastPlacement(userId)

                val update = objectMapper.writeValueAsString(
                    mapOf(
                        "type" to "pixel",
                        "x" to x,
                        "y" to y,
                        "color" to color,
                    )
                )
                broadcastMessage(TextMessage(update))
            } finally {
                lock.unlock()
            }
        } catch (e: Exception) {
            println("Error processing message: ${e.message}")
            session.sendMessage(TextMessage("""{"error":"Invalid request"}"""))
        }
    }

    private fun broadcastMessage(message: TextMessage) {
        val iterator = activeSessions.iterator()
        while (iterator.hasNext()) {
            val session = iterator.next()
            try {
                if (session.isOpen) {
                    session.sendMessage(message)
                } else {
                    iterator.remove()
                }
            } catch (e: Exception) {
                println("Broadcast failed for ${session.id} error: " + e.message)
                iterator.remove()
            }
        }
    }

    private fun getAuthenticatedUserId(session: WebSocketSession): String {
        return session.attributes["userId"] as? String ?: throw IllegalStateException("Unauthenticated session")
    }
}
