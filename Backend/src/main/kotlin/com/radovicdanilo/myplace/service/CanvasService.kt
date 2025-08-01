package com.radovicdanilo.myplace.service

import com.radovicdanilo.myplace.constants.CanvasConstants.CANVAS_HEIGHT
import com.radovicdanilo.myplace.constants.CanvasConstants.CANVAS_WIDTH
import com.radovicdanilo.myplace.constants.CanvasConstants.COLOR_BITS
import org.springframework.data.redis.connection.BitFieldSubCommands
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class CanvasService(
    private val redisTemplate: RedisTemplate<String, ByteArray>
) {
    private val canvasKey = "canvas:bitfield"

    fun getFullCanvas(): ByteArray {
        return redisTemplate.opsForValue().get(canvasKey)
            ?: ByteArray(CANVAS_WIDTH * CANVAS_HEIGHT * COLOR_BITS / 8) { 0 }
    }

    fun setPixel(x: Int, y: Int, color: Int) {
        val offset = (y * CANVAS_WIDTH + x) * COLOR_BITS

        redisTemplate.execute { conn ->
            val keyBytes = redisTemplate.stringSerializer.serialize(canvasKey)!!
            val cmd = BitFieldSubCommands.create().set(BitFieldSubCommands.BitFieldType.unsigned(COLOR_BITS))
                .valueAt(offset.toLong()).to(color.toLong())
            conn.stringCommands().bitField(keyBytes, cmd)
        }

        println("Pixel set: $x $y $color")
    }

}
