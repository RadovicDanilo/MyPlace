package com.radovicdanilo.myplace.repository

import com.radovicdanilo.myplace.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(string: String): User?
    fun existsByUsername(string: String): Boolean
}
