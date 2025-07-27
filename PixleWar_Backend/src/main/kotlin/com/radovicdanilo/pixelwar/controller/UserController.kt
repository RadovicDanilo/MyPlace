package com.radovicdanilo.pixelwar.controller

import com.radovicdanilo.pixelwar.config.security.CheckSecurity
import com.radovicdanilo.pixelwar.dto.create.CreateUserDto
import com.radovicdanilo.pixelwar.dto.token.TokenRequestDto
import com.radovicdanilo.pixelwar.dto.token.TokenResponseDto
import com.radovicdanilo.pixelwar.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/register")
    fun registerClient(@Valid @RequestBody createUserDto: CreateUserDto): ResponseEntity<Void> {
        return if (userService.register(createUserDto)) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody tokenRequestDto: TokenRequestDto): ResponseEntity<TokenResponseDto> {
        return try {
            val user = userService.login(tokenRequestDto)
            ResponseEntity.ok(user)
        } catch (_: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @CheckSecurity
    @PostMapping("/pfp")
    fun addPfp(
        @RequestParam("file") file: MultipartFile, @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Void> {
        val result = userService.saveProfilePicture(file, authHeader)
        return if (result) ResponseEntity.ok().build() else ResponseEntity.badRequest().build()
    }

    @GetMapping("/pfp/{id}")
    fun getPfp(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val image = userService.loadProfilePicture(id)
        return image ?: ResponseEntity.notFound().build()
    }
}

