package com.radovicdanilo.pixelwar.dto.pixel

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class PaintReq(
    @field:NotBlank @field:Min(0) @field:Max(1023) val x: Int,
    @field:NotBlank @field:Min(0) @field:Max(1023) val y: Int,
    @field:NotBlank @field:Min(0) @field:Max(15) val color: Int,
)