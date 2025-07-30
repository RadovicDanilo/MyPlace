package com.radovicdanilo.pixelwar.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.TimeUnit

@Service
class UserCooldownService(
    private val redisTemplate: RedisTemplate<String, String>
) {
    companion object {
        private const val COOLDOWN_SECONDS = 1L
        private const val COOLDOWN_KEY_PREFIX = "pixel:cooldown:"
        private const val MAX_RETRIES = 3
    }

    fun canPlacePixel(userId: String): Boolean {
        return try {
            val key = getKey(userId)
            val lastPlacement = redisTemplate.opsForValue().get(key)
            lastPlacement == null || Instant.parse(lastPlacement).isBefore(Instant.now().minusSeconds(COOLDOWN_SECONDS))
        } catch (_: Exception) {
            // Fail open in case of Redis issues to avoid blocking users
            true
        }
    }

    fun updateLastPlacement(userId: String) {
        var retries = 0
        while (retries < MAX_RETRIES) {
            try {
                redisTemplate.opsForValue().set(
                    getKey(userId), Instant.now().toString(), COOLDOWN_SECONDS, TimeUnit.SECONDS
                )
                return
            } catch (e: Exception) {
                retries++
                if (retries == MAX_RETRIES) {
                    println("Failed to update cooldown after $MAX_RETRIES attempts: ${e.message}")
                }
            }
        }
    }

    private fun getKey(userId: String): String {
        return COOLDOWN_KEY_PREFIX + userId
    }
}
