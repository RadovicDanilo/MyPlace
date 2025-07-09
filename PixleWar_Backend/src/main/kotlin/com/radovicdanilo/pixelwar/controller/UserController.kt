package com.radovicdanilo.pixelwar.controller

import com.radovicdanilo.pixelwar.dto.create.CreateUserDto
import com.radovicdanilo.pixelwar.dto.token.TokenRequestDto
import com.radovicdanilo.pixelwar.dto.token.TokenResponseDto
import com.radovicdanilo.pixelwar.security.CheckSecurity
import com.radovicdanilo.pixelwar.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/register")
    fun registerClient(@RequestBody createUserDto: CreateUserDto): ResponseEntity<Void> {
        return if (userService.register(createUserDto)) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody tokenRequestDto: TokenRequestDto): ResponseEntity<TokenResponseDto> {
        return userService.login(tokenRequestDto)?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }

    @CheckSecurity
    @PostMapping("/pfp")
    fun addPfp() {

    }

    @PostMapping("/pfp/{id}")
    fun getPfp() {

    }
}

