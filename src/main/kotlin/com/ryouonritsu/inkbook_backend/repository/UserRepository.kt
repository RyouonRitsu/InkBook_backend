package com.ryouonritsu.inkbook_backend.repository

import com.ryouonritsu.inkbook_backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
}