package com.radovicdanilo.pixelwar.security.service

import io.jsonwebtoken.Claims
import org.springframework.stereotype.Service

interface TokenService {
    fun generate(claims: Claims): String

    fun parseToken(jwt: String): Claims

    fun getId(token: String): Long
}
