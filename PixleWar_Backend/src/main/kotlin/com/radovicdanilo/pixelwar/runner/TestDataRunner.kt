package com.radovicdanilo.pixelwar.runner

import org.springframework.boot.CommandLineRunner
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class TestDataRunner(
    private val redisTemplate: StringRedisTemplate
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        println("INITIALIZING CANVAS ")
        val canvasKey = "canvas:bitfield"

        if (redisTemplate.hasKey(canvasKey)) return

        // 1024 * 1024 * 4 bits = 524_288 Bytes
        val emptyCanvas = ByteArray(524_288) { 0 }

        redisTemplate.opsForValue().set(canvasKey, String(emptyCanvas, Charsets.ISO_8859_1))
    }
}
