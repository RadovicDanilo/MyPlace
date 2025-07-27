package com.radovicdanilo.pixelwar.security

import com.radovicdanilo.pixelwar.config.security.Roles
import com.radovicdanilo.pixelwar.config.security.service.TokenServiceImpl
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class TokenServiceImplTest {

    private lateinit var tokenService: TokenServiceImpl

    private val secretKeyBase64 = Base64.getEncoder().encodeToString(ByteArray(64) { it.toByte() })

    @BeforeEach
    fun setup() {
        tokenService = TokenServiceImpl(jwtSecret = secretKeyBase64)
    }

    fun defaultClaims(): Claims = Jwts.claims().setSubject("username").setIssuedAt(Date()).apply {
        this["role"] = Roles.USER
        this["userId"] = 1L
    }

    @Test
    fun `generate and parseToken should work correctly`() {
        val claims: Claims = defaultClaims()

        val token = tokenService.generate(claims)
        assertNotNull(token, "Generated token should not be null or empty")
        assertTrue(token.isNotEmpty(), "Generated token should not be empty")

        val parsedClaims = tokenService.parseToken(token)
        assertEquals("username", parsedClaims.subject)
        assertEquals(1L, (parsedClaims["userId"] as Number).toLong())
    }

    @Test
    fun `getId should correctly extract id from token`() {
        val claims: Claims = defaultClaims()

        val token = tokenService.generate(claims)

        val id = tokenService.getId(token)
        assertEquals(1L, id)
    }

    @Test
    fun `getId should convert id from different types`() {
        // Test with Int
        val claimsInt: Claims = defaultClaims()
        claimsInt["userId"] = 99
        claimsInt.subject = "subjectInt"
        val tokenInt = tokenService.generate(claimsInt)
        assertEquals(99L, tokenService.getId(tokenInt))

        // Test with String
        val claimsString: Claims = defaultClaims()
        claimsString["userId"] = "12345"
        claimsString.subject = "subjectString"
        val tokenString = tokenService.generate(claimsString)
        assertEquals(12345L, tokenService.getId(tokenString))
    }

    @Test
    fun `generate should fail if jwtSecret is null or invalid`() {
        val tokenServiceInvalid = TokenServiceImpl(jwtSecret = null)
        val claims: Claims = defaultClaims()

        assertThrows<NullPointerException> {
            tokenServiceInvalid.generate(claims)
        }
    }
}
