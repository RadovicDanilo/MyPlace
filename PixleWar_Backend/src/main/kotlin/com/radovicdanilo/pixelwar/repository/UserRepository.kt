package com.radovicdanilo.pixelwar.repository

import com.radovicdanilo.pixelwar.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service

@Service
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(string: String): User?
    fun existsByUsername(string: String): Boolean
}
