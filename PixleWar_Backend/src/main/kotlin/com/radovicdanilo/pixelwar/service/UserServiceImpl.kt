package com.radovicdanilo.pixelwar.service

import com.radovicdanilo.pixelwar.domain.User
import com.radovicdanilo.pixelwar.dto.create.CreateUserDto
import com.radovicdanilo.pixelwar.dto.token.TokenRequestDto
import com.radovicdanilo.pixelwar.dto.token.TokenResponseDto
import com.radovicdanilo.pixelwar.repository.UserRepository
import com.radovicdanilo.pixelwar.security.Roles
import com.radovicdanilo.pixelwar.security.service.TokenService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import java.util.Date

@Service
class UserServiceImpl(
    private val tokenService: TokenService, private val userRepository: UserRepository
) : UserService {

    override fun login(tokenRequestDto: TokenRequestDto): TokenResponseDto {
        val username = tokenRequestDto.username
        val rawPassword = tokenRequestDto.password

        val user: User =
            userRepository.findByUsername(username) ?: throw DataIntegrityViolationException("User not found")

        if (!BCrypt.checkpw(rawPassword, user.passwordHash)) {
            throw Exception("Invalid password")
        }

        val claims: Claims = Jwts.claims().setSubject(username).setIssuedAt(Date()).apply {
            this["role"] = Roles.USER
            this["id"] = user.id
        }

        val token = tokenService.generate(claims)
        return TokenResponseDto(token)
    }

    override fun register(dto: CreateUserDto): Boolean {
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
}