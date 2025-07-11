package com.radovicdanilo.pixelwar.controller

import com.radovicdanilo.pixelwar.dto.create.CreateUserDto
import com.radovicdanilo.pixelwar.dto.token.TokenRequestDto
import com.radovicdanilo.pixelwar.dto.token.TokenResponseDto
import com.radovicdanilo.pixelwar.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile
import kotlin.test.assertEquals

@Suppress("SpellCheckingInspection")
class UserControllerTest {

    private lateinit var userService: UserService
    private lateinit var controller: UserController

    @BeforeEach
    fun setup() {
        userService = mock(UserService::class.java)
        controller = UserController(userService)
    }

    // region: register

    @Test
    fun userController_registerClient_success() {
        // Arrange
        val dto = CreateUserDto("validuser", "securepassword")
        whenever(userService.register(dto)).thenReturn(true)

        // Act
        val response: ResponseEntity<Void> = controller.registerClient(dto)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun userController_registerClient_userAlreadyExists() {
        // Arrange
        val dto = CreateUserDto("existingUser", "password")
        whenever(userService.register(dto)).thenReturn(false)

        // Act
        val response: ResponseEntity<Void> = controller.registerClient(dto)

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun userController_registerClient_usernameTooShort() {
        // Arrange
        val dto = CreateUserDto("a", "validPassword123")
        whenever(userService.register(dto)).thenReturn(false) // simulate validation fail

        // Act
        val response = controller.registerClient(dto)

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun userController_registerClient_passwordTooShort() {
        // Arrange
        val dto = CreateUserDto("validuser", "123")
        whenever(userService.register(dto)).thenReturn(false)

        // Act
        val response = controller.registerClient(dto)

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun userController_registerClient_passwordAndUsernameTooLong() {
        // Arrange
        val dto = CreateUserDto(
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        )
        whenever(userService.register(dto)).thenReturn(false)

        // Act
        val response = controller.registerClient(dto)

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    // endregion

    // region: login

    @Test
    fun userController_login_sucess() {
        // Arange
        val dto = TokenRequestDto("username", "password")
        whenever(userService.login(dto)).thenReturn(TokenResponseDto("jwt-token"))

        // Act
        val response = controller.login(dto)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun userController_login_failed() {
        // Arrange
        val dto = TokenRequestDto("username", "password")
        whenever(userService.login(dto)).thenThrow(RuntimeException("Login failed"))

        // Act
        val response = controller.login(dto)

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    // endregion

    // region: addPfp

    @Test
    fun userController_addPfp_success() {
        // Arrange
        val file = mock(MultipartFile::class.java)
        val authHeader = "Bearer token"
        whenever(userService.saveProfilePicture(file, authHeader)).thenReturn(true)

        // Act
        val response = controller.addPfp(file, authHeader)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun userController_addPfp_failure_returnsBadRequest() {
        // Arrange
        val file = mock(MultipartFile::class.java)
        val authHeader = "Bearer token"
        whenever(userService.saveProfilePicture(file, authHeader)).thenReturn(false)

        // Act
        val response = controller.addPfp(file, authHeader)

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    // endregion

    // region: getPfp

    @Test
    fun userController_getPfp_existingUser_returnsImage() {
        // Arrange
        val id = 1L
        val imageBytes = byteArrayOf(1, 2, 3)
        whenever(userService.loadProfilePicture(id)).thenReturn(ResponseEntity.ok(imageBytes))

        // Act
        val response = controller.getPfp(id)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(imageBytes.toList(), response.body?.toList())
    }

    @Test
    fun userController_getPfp_nonexistentUser_returnsNotFound() {
        // Arrange
        val id = 99L
        whenever(userService.loadProfilePicture(id)).thenReturn(null)

        // Act
        val response = controller.getPfp(id)

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    // endregion
}
