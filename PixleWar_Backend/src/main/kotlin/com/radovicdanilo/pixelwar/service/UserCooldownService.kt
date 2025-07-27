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
        private const val COOLDOWN_SECONDS = 60L
        private const val COOLDOWN_KEY_PREFIX = "pixel:cooldown:"
    }

    fun canPlacePixel(userId: String): Boolean {
        val lastPlacement = redisTemplate.opsForValue().get(getKey(userId))
        return lastPlacement == null || Instant.parse(lastPlacement).plusSeconds(COOLDOWN_SECONDS)
            .isBefore(Instant.now())
    }

    fun updateLastPlacement(userId: String) {
        redisTemplate.opsForValue().set(
            getKey(userId), Instant.now().toString(), COOLDOWN_SECONDS, TimeUnit.SECONDS
        )
    }

    private fun getKey(userId: String): String {
        return COOLDOWN_KEY_PREFIX + userId
    }
}