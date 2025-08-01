package com.radovicdanilo.myplace.service

import com.radovicdanilo.myplace.dto.create.CreateUserDto
import com.radovicdanilo.myplace.dto.token.TokenRequestDto
import com.radovicdanilo.myplace.dto.token.TokenResponseDto
import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile

interface UserService {
    fun login(tokenRequestDto: TokenRequestDto): TokenResponseDto
    fun register(createUserDto: CreateUserDto): Boolean

    fun saveProfilePicture(file: MultipartFile, authHeader: String): Boolean
    fun loadProfilePicture(userId: Long): ResponseEntity<ByteArray>?
}
