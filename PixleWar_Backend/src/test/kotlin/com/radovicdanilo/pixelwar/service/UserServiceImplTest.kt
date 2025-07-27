package com.radovicdanilo.pixelwar.service

import com.radovicdanilo.pixelwar.domain.User
import com.radovicdanilo.pixelwar.dto.create.CreateUserDto
import com.radovicdanilo.pixelwar.dto.token.TokenRequestDto
import com.radovicdanilo.pixelwar.repository.UserRepository
import com.radovicdanilo.pixelwar.config.security.service.TokenService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.test.*

class UserServiceImplTest {

    private lateinit var userRepository: UserRepository
    private lateinit var tokenService: TokenService
    private lateinit var service: UserServiceImpl

    @BeforeEach
    fun setup() {
        userRepository = mock()
        tokenService = mock()
        service = UserServiceImpl(tokenService, userRepository)
    }

    // region: register

    @Test
    fun userService_register_success_returnsTrue() {
        // Arrange
        val dto = CreateUserDto("validUser", "strongPassword")
        whenever(userRepository.existsByUsername(dto.username)).thenReturn(false)
        whenever(userRepository.save(any<User>())).thenReturn(User(1L, dto.username, "hashed"))

        // Act
        val result = service.register(dto)

        // Assert
        assertTrue(result)
    }

    @Test
    fun userService_register_userAlreadyExists_returnsFalse() {
        // Arrange
        val dto = CreateUserDto("existingUser", "pass")
        whenever(userRepository.existsByUsername(dto.username)).thenReturn(true)

        // Act
        val result = service.register(dto)

        // Assert
        assertFalse(result)
    }

    @Test
    fun userService_register_dataIntegrityViolation_returnsFalse() {
        // Arrange
        val dto = CreateUserDto("valid", "pass")
        whenever(userRepository.existsByUsername(dto.username)).thenReturn(false)
        whenever(userRepository.save(any<User>())).thenThrow(DataIntegrityViolationException("error"))

        // Act
        val result = service.register(dto)

        // Assert
        assertFalse(result)
    }

    // endregion

    // region: login

    @Test
    fun userService_login_success_returnsToken() {
        // Arrange
        val username = "username11"
        val password = "password11"
        val hashed = BCrypt.hashpw(password, BCrypt.gensalt())
        val user = User(1L, username, hashed)

        whenever(userRepository.findByUsername(username)).thenReturn(user)
        whenever(tokenService.generate(any())).thenReturn("jwt-token")

        // Act
        val result = service.login(TokenRequestDto(username, password))

        // Assert
        assertEquals("jwt-token", result.token)
    }

    @Test
    fun userService_login_userNotFound_throwsException() {
        // Arrange
        val dto = TokenRequestDto("nonexistent", "pass")
        whenever(userRepository.findByUsername(dto.username)).thenReturn(null)

        // Act & Assert
        val ex = assertFailsWith<DataIntegrityViolationException> {
            service.login(dto)
        }
        assertEquals("User not found", ex.message)
    }

    @Test
    fun userService_login_invalidPassword_throwsException() {
        // Arrange
        val username = "user"
        val password = "wrong"
        val correctHash = BCrypt.hashpw("right", BCrypt.gensalt())
        val user = User(1L, username, correctHash)

        whenever(userRepository.findByUsername(username)).thenReturn(user)

        // Act & Assert
        val ex = assertFailsWith<Exception> {
            service.login(TokenRequestDto(username, password))
        }
        assertEquals("Invalid password", ex.message)
    }

    // endregion

    // region: saveProfilePicture

    @Test
    fun userService_saveProfilePicture_validJpg_returnsTrue() {
        // Arrange
        val file = mock<MultipartFile>()
        whenever(file.isEmpty).thenReturn(false)
        whenever(file.size).thenReturn(1024L)
        whenever(file.contentType).thenReturn("image/jpeg")
        whenever(tokenService.getId("Bearer abc")).thenReturn(1L)
        doNothing().whenever(file).transferTo(any<File>())

        // Act
        val result = service.saveProfilePicture(file, "Bearer abc")

        // Assert
        assertTrue(result)
    }

    @Test
    fun userService_saveProfilePicture_invalidType_returnsFalse() {
        // Arrange
        val file = mock<MultipartFile>()
        whenever(file.isEmpty).thenReturn(false)
        whenever(file.size).thenReturn(1024L)
        whenever(file.contentType).thenReturn("application/pdf")

        // Act
        val result = service.saveProfilePicture(file, "Bearer xyz")

        // Assert
        assertFalse(result)
    }

    // endregion

    // region: loadProfilePicture

    @Test
    fun userService_loadProfilePicture_existing_returnsBytes() {
        // Arrange
        val userId = 1L
        val filePath = Path("uploads/profile-pictures/$userId.jpg")
        Files.createDirectories(filePath.parent)
        Files.write(filePath, byteArrayOf(10, 20, 30))

        // Act
        val response = service.loadProfilePicture(userId)

        // Assert
        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("image/jpeg", response.headers.getFirst("Content-Type"))

        // Cleanup
        Files.deleteIfExists(filePath)
    }

    @Test
    fun userService_loadProfilePicture_notFound_returnsNull() {
        // Act
        val result = service.loadProfilePicture(-1L)

        // Assert
        assertNull(result)
    }

    // endregion
}
