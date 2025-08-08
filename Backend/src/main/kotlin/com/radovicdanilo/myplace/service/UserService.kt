package com.radovicdanilo.myplace.service

import com.radovicdanilo.myplace.config.security.Roles
import com.radovicdanilo.myplace.config.security.service.TokenService
import com.radovicdanilo.myplace.domain.User
import com.radovicdanilo.myplace.dto.create.CreateUserDto
import com.radovicdanilo.myplace.dto.token.TokenRequestDto
import com.radovicdanilo.myplace.dto.token.TokenResponseDto
import com.radovicdanilo.myplace.repository.UserRepository
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@Service
class UserService(
    private val tokenService: TokenService, private val userRepository: UserRepository
) {

    fun login(tokenRequestDto: TokenRequestDto): TokenResponseDto {
        val username = tokenRequestDto.username
        val rawPassword = tokenRequestDto.password

        val user: User =
            userRepository.findByUsername(username) ?: throw DataIntegrityViolationException("User not found")

        if (!BCrypt.checkpw(rawPassword, user.passwordHash)) {
            throw Exception("Invalid password")
        }

        val claims: Claims = Jwts.claims().setSubject(username).setIssuedAt(Date()).apply {
            this["role"] = Roles.USER
            this["userId"] = user.id
        }

        val token = tokenService.generate(claims)

        return TokenResponseDto(token)
    }

    fun register(dto: CreateUserDto): Boolean {
        if (userRepository.existsByUsername(dto.username)) {
            return false
        }

        val hashedPassword = BCrypt.hashpw(dto.password, BCrypt.gensalt())
        val user = User(
            username = dto.username,
            passwordHash = hashedPassword,
        )

        return try {
            userRepository.save(user)
            true
        } catch (_: DataIntegrityViolationException) {
            false
        }
    }

    private val allowedContentTypes = listOf("image/jpeg", "image/png")
    private val maxFileSizeBytes = 5 * 1024 * 1024L

    fun saveProfilePicture(file: MultipartFile, authHeader: String): Boolean {
        if (file.isEmpty || file.size > maxFileSizeBytes || file.contentType !in allowedContentTypes) {
            return false
        }

        val userId = tokenService.getId(authHeader)
        val ext = when (file.contentType) {
            "image/png" -> "png"
            else -> "jpg"
        }

        val directory = File(System.getProperty("user.dir"), "uploads/profile-pictures")
        if (!directory.exists()) directory.mkdirs()

        val targetFile = File(directory, "$userId.$ext")
        file.transferTo(targetFile)

        return true
    }

    fun loadProfilePicture(userId: Long): ResponseEntity<ByteArray>? {
        val dir = Paths.get("uploads/profile-pictures")
        val possibleExtensions = listOf("jpg", "png")

        possibleExtensions.forEach { ext ->
            val path = dir.resolve("$userId.$ext")
            if (!Files.exists(path)) return@forEach

            val bytes = Files.readAllBytes(path)
            val contentType = when (ext) {
                "png" -> "image/png"
                else -> "image/jpeg"
            }
            return ResponseEntity.ok().header("Content-Type", contentType).body(bytes)
        }

        return null
    }
}