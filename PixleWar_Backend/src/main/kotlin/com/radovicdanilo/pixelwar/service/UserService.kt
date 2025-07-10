package com.radovicdanilo.pixelwar.service

import com.radovicdanilo.pixelwar.dto.create.CreateUserDto
import com.radovicdanilo.pixelwar.dto.token.TokenRequestDto
import com.radovicdanilo.pixelwar.dto.token.TokenResponseDto
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

interface UserService {
    fun login(tokenRequestDto: TokenRequestDto): TokenResponseDto
    fun register(createUserDto: CreateUserDto): Boolean

    fun saveProfilePicture(file: MultipartFile, authHeader: String): Boolean
    fun loadProfilePicture(userId: Long): ResponseEntity<ByteArray>?
}
