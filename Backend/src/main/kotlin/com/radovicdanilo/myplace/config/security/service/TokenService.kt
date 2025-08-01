package com.radovicdanilo.myplace.config.security.service

import io.jsonwebtoken.Claims

interface TokenService {
    fun generate(claims: Claims): String

    fun parseToken(jwt: String): Claims

    fun getId(token: String): Long
}
