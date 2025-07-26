package com.radovicdanilo.pixelwar.runner

import org.springframework.boot.CommandLineRunner
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class TestDataRunner(
    private val redisTemplate: RedisTemplate<String, ByteArray>
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val canvasKey = "canvas:bitfield"

        if (redisTemplate.hasKey(canvasKey)) return

        // 1024 * 1024 * 4 bits = 524_288 Bytes
        val emptyCanvas = ByteArray(524_288) { 0 }

        redisTemplate.opsForValue().set(canvasKey, emptyCanvas)
    }
}
