package com.radovicdanilo.pixelwar.controller

import com.radovicdanilo.pixelwar.dto.pixel.PaintReq
import com.radovicdanilo.pixelwar.service.CanvasService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/canvas")
class CanvasController(
    val canvasService: CanvasService
) {
    @GetMapping("/raw", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun getCanvasRaw(): ResponseEntity<ByteArray> {
        val raw = canvasService.getFullCanvas()
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(raw.toByteArray())
    }

    @PostMapping("/pixel")
    fun setPixel(
        @RequestParam paintReq: PaintReq,
    ): ResponseEntity<String> {
        canvasService.setPixel(paintReq)
        return ResponseEntity.ok("ok")
    }
}
