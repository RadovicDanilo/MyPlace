package com.radovicdanilo.pixelwar.socket

import com.fasterxml.jackson.databind.ObjectMapper
import com.radovicdanilo.pixelwar.constants.CanvasConstants.CANVAS_HEIGHT
import com.radovicdanilo.pixelwar.constants.CanvasConstants.CANVAS_WIDTH
import com.radovicdanilo.pixelwar.constants.CanvasConstants.MAX_COLOR
import com.radovicdanilo.pixelwar.service.CanvasService
import com.radovicdanilo.pixelwar.service.UserCooldownService
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
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
) : TextWebSocketHandler(), DisposableBean {

    private val activeSessions = ConcurrentHashMap.newKeySet<WebSocketSession>()
    private val lock = ReentrantLock()
    private val maxSessions = 10_000

    override fun afterConnectionEstablished(session: WebSocketSession) {
        if (activeSessions.size >= maxSessions) {
            session.close(CloseStatus.SERVICE_OVERLOAD)
            return
        }
        activeSessions.add(session)
        println("WebSocket connected: ${session.id}")

        session.sendMessage(BinaryMessage(canvasService.getFullCanvas()))
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
                    println("Cooldown active for user $userId")
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
            println("Error processing message: ${e.message} cause: ${e.cause} ")
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
        return session.attributes["userId"]?.toString() ?: throw IllegalStateException("Unauthenticated session")
    }

    @PreDestroy
    override fun destroy() {
        gracefulShutdown()
    }

    private fun gracefulShutdown() {
        println("Initiating WebSocket graceful shutdown...")

        activeSessions.forEach { session ->
            try {
                if (session.isOpen) {
                    session.close(CloseStatus.SERVICE_RESTARTED)
                }
            } catch (e: Exception) {
                println("Error closing session ${session.id} error: " + e.message)
            }
        }

        activeSessions.clear()

        println("WebSocket shutdown completed")
    }
}
