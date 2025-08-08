package com.radovicdanilo.myplace.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

@Service
class UserCooldownService(
    private val redisTemplate: RedisTemplate<String, String>
) {
    companion object {
        private const val DEFAULT_COOLDOWN_SECONDS = 60L
        private const val COOLDOWN_KEY_PREFIX = "pixel:cooldown:"
        private const val MAX_RETRIES = 3
    }

    private var cooldownSeconds = AtomicLong(DEFAULT_COOLDOWN_SECONDS)

    fun changeCooldown(cooldownSeconds: Long) {
        this.cooldownSeconds.set(cooldownSeconds)
    }

    fun canPlacePixel(userId: String): Boolean {
        return try {
            val key = getKey(userId)
            val lastPlacement = redisTemplate.opsForValue().get(key)
            lastPlacement == null || Instant.parse(lastPlacement)
                .isBefore(Instant.now().minusSeconds(cooldownSeconds.get()))
        } catch (_: Exception) {
            // Fail open in case of Redis issues to avoid blocking users
            true
        }
    }

    fun updateLastPlacement(userId: String) {
        (0 until 3).forEach { retries ->
            try {
                redisTemplate.opsForValue().set(
                    getKey(userId), Instant.now().toString(), cooldownSeconds.get(), TimeUnit.SECONDS
                )
                return
            } catch (e: Exception) {
                if (retries > MAX_RETRIES) {
                    println("Failed to update cooldown after $MAX_RETRIES attempts: ${e.message}")
                }
            }
        }
    }

    private fun getKey(userId: String): String {
        return COOLDOWN_KEY_PREFIX + userId
    }
}
