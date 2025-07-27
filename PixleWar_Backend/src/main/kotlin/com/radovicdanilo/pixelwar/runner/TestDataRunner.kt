package com.radovicdanilo.pixelwar.runner

import com.radovicdanilo.pixelwar.constants.CanvasConstants.CANVAS_HEIGHT
import com.radovicdanilo.pixelwar.constants.CanvasConstants.CANVAS_WIDTH
import com.radovicdanilo.pixelwar.constants.CanvasConstants.COLOR_BITS
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

        val emptyCanvas = ByteArray(CANVAS_HEIGHT * CANVAS_WIDTH * COLOR_BITS / 8) { 0 }

        redisTemplate.opsForValue().set(canvasKey, emptyCanvas)
    }
}
