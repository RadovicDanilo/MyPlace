package com.radovicdanilo.myplace.integration

import com.radovicdanilo.myplace.dto.token.TokenResponseDto
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.LinkedMultiValueMap
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserIntegrationTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    private val baseUrl = "/user"
    private val username = "testuser"
    private val password = "password123"
    private val testUserId = 1L

    @Test
    fun `register, login, upload and retrieve profile picture - full flow`() {
        println("Starting user integration flow test")

        registerUser()
        val token = loginUserAndGetToken()
        uploadProfilePicture(token)
        downloadAndVerifyProfilePicture()

        println("Integration test completed successfully")
    }

    private fun registerUser() {
        println("Registering user...")
        val request = mapOf("username" to username, "password" to password)
        val response = restTemplate.postForEntity("$baseUrl/register", request, Void::class.java)
        assertEquals(HttpStatus.OK, response.statusCode, "Registration failed")
    }

    private fun loginUserAndGetToken(): String {
        println("Logging in user...")
        val request = mapOf("username" to username, "password" to password)
        val response = restTemplate.postForEntity("$baseUrl/login", request, TokenResponseDto::class.java)
        assertEquals(HttpStatus.OK, response.statusCode, "Login failed")
        return response.body?.token ?: error("No token returned in login response")
    }

    private fun uploadProfilePicture(token: String) {
        println("Uploading profile picture...")
        val resource = ClassPathResource("test-pfp.jpg")
        val headers = HttpHeaders().apply {
            contentType = MediaType.MULTIPART_FORM_DATA
            setBearerAuth(token)
        }

        val body = LinkedMultiValueMap<String, Any>().apply {
            add("file", resource)
        }

        val requestEntity = HttpEntity(body, headers)
        val response = restTemplate.postForEntity("$baseUrl/pfp", requestEntity, Void::class.java)
        assertEquals(HttpStatus.OK, response.statusCode, "Profile picture upload failed")
    }

    private fun downloadAndVerifyProfilePicture() {
        println("Retrieving profile picture...")
        val response = restTemplate.getForEntity("$baseUrl/pfp/$testUserId", ByteArray::class.java)
        assertEquals(HttpStatus.OK, response.statusCode, "Failed to retrieve profile picture")
        assertEquals(MediaType.IMAGE_JPEG, response.headers.contentType, "Unexpected content type")
        assertNotNull(response.body, "Profile picture body is null")
        assertTrue(response.body!!.isNotEmpty(), "Profile picture content is empty")
    }
}
