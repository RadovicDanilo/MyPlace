package com.radovicdanilo.pixelwar.dto.token

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class TokenRequestDto(
    @field:NotBlank @field:Size(min = 3, max = 32) val username: String,

    @field:NotBlank @field:Size(min = 8,max= 128) val password: String
)
