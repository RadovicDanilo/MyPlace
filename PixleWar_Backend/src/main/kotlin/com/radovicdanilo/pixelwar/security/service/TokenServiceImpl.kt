package com.radovicdanilo.pixelwar.security.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Service
class TokenServiceImpl(
    @Value("\${oauth.jwt.secret}") private val jwtSecret: String? = null
) : TokenService {

    private val secretKey: SecretKey
        get() = SecretKeySpec(Base64.getDecoder().decode(jwtSecret), SignatureAlgorithm.HS512.jcaName)

    override fun generate(claims: Claims): String {
        val currentDate = Date()
        val expirationDate = Date(currentDate.time + EXPIRATION_TIME)

        return Jwts.builder().setSubject(claims.subject).setClaims(claims).setIssuedAt(currentDate)
            .setExpiration(expirationDate).signWith(secretKey).compact()
    }

    override fun parseToken(token: String): Claims {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token.removePrefix("Bearer ")).body
    }

    override fun getId(token: String): Long {
        val idClaim = parseToken(token)["id"]
        return when (idClaim) {
            is Long -> idClaim
            is Int -> idClaim.toLong()
            is String -> idClaim.toLong()
            else -> throw IllegalArgumentException("Invalid ID in token")
        }
    }

    companion object {
        private const val EXPIRATION_TIME: Long = 864000000 // 10 day
    }
}
