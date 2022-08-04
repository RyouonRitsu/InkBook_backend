package com.ryouonritsu.inkbook_backend.service

import com.ryouonritsu.inkbook_backend.entity.User

interface UserService {
    operator fun get(user_id: Long): User?
    fun selectUserByEmail(email: String): User?
    fun selectUserByUsername(username: String): User?
    operator fun plus(user: User)
    operator fun invoke(user: User)
}