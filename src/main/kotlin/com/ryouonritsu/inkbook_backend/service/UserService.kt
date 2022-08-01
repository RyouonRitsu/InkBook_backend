package com.ryouonritsu.inkbook_backend.service

import com.ryouonritsu.inkbook_backend.entity.User

interface UserService {
    fun selectUserByUserId(userId: Long): Map<String, String>?
    fun selectUserByEmail(email: String): Map<String, String>?
    fun selectUserByUsername(username: String): Map<String, String>?
    fun registerNewUser(user: User)
}