package com.radovicdanilo.pixelwar.service

import com.radovicdanilo.pixelwar.dto.create.CreateUserDto
import com.radovicdanilo.pixelwar.dto.token.TokenRequestDto
import com.radovicdanilo.pixelwar.dto.token.TokenResponseDto
import org.springframework.stereotype.Service

@Service
interface UserService {
    fun login(tokenRequestDto: TokenRequestDto): TokenResponseDto
    fun register(createUserDto: CreateUserDto): Boolean
}
