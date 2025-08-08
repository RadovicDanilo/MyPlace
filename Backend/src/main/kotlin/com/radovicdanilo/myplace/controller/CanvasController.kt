package com.radovicdanilo.myplace.controller

import com.radovicdanilo.myplace.service.CanvasService
import com.radovicdanilo.myplace.service.UserCooldownService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/canvas")
class CanvasController(
    val canvasService: CanvasService,
    val userCooldownService: UserCooldownService,
) {
    @GetMapping("/raw", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun getCanvasRaw(): ResponseEntity<ByteArray> {
        val raw = canvasService.getFullCanvas()
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(raw)
    }

    @PostMapping("/cooldown/set/{cooldownSeconds}")
    fun login(@PathVariable cooldownSeconds: Int): ResponseEntity<Any> {
        userCooldownService.changeCooldown(cooldownSeconds.toLong())
        return ResponseEntity.ok().build<Any>()
    }
}
