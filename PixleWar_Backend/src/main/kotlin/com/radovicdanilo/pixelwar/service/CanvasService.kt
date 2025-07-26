package com.radovicdanilo.pixelwar.service

import org.springframework.data.redis.connection.BitFieldSubCommands
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class CanvasService(
    private val redisTemplate: RedisTemplate<String, ByteArray>
) {
    private val canvasKey = "canvas:bitfield"

    fun getFullCanvas(): ByteArray {
        return redisTemplate.opsForValue().get(canvasKey) ?: ByteArray(524_288) { 0 }
    }

    fun setPixel(x: Int, y: Int, color: Int) {
        val offset = x + y * 1024L

        redisTemplate.execute { conn ->
            val keyBytes = redisTemplate.stringSerializer.serialize(canvasKey)!!
            val bitfieldArgs =
                BitFieldSubCommands.create().set(BitFieldSubCommands.BitFieldType.unsigned(4)).valueAt(offset)
                    .to(color.toLong())
            conn.stringCommands().bitField(keyBytes, bitfieldArgs)
        }
    }
}
