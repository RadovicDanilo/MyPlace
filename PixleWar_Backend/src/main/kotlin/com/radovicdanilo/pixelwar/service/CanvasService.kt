package com.radovicdanilo.pixelwar.service

import org.springframework.data.redis.connection.BitFieldSubCommands
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class CanvasService(
    private val redisTemplate: RedisTemplate<String, ByteArray>
) {
    private val canvasKey = "canvas:bitfield"
    private val width = 1024
    private val height = 1024
    private val bitsPerPixel = 4

    fun getFullCanvas(): ByteArray {
        return redisTemplate.opsForValue().get(canvasKey) ?: ByteArray(width * height * bitsPerPixel / 8) { 0 }
    }

    fun setPixel(x: Int, y: Int, color: Int) {
        val offset = (y * width + x) * bitsPerPixel

        redisTemplate.execute { conn ->
            val keyBytes = redisTemplate.stringSerializer.serialize(canvasKey)!!
            val cmd = BitFieldSubCommands.create().set(BitFieldSubCommands.BitFieldType.unsigned(bitsPerPixel))
                .valueAt(offset.toLong()).to(color.toLong())
            conn.stringCommands().bitField(keyBytes, cmd)
        }
    }

}
