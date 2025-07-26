package com.radovicdanilo.pixelwar.service

import com.radovicdanilo.pixelwar.dto.pixel.PaintReq
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class CanvasService(
    private val redisTemplate: StringRedisTemplate
) {
    private val canvasKey = "canvas:bitfield"

    fun getFullCanvas(): String {
        return redisTemplate.opsForValue().get(canvasKey).toString()
    }

    fun setPixel(paintReq: PaintReq) {
        val x = paintReq.x
        val y = paintReq.y
        val offset = x + y * 1024

        val color = paintReq.color
    }
}
