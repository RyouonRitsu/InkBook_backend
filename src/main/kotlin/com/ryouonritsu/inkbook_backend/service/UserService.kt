package com.ryouonritsu.inkbook_backend.service

import com.ryouonritsu.inkbook_backend.entity.User

interface UserService {
    fun selectUserByUserId(user_id: Long): User?
    fun selectUserByEmail(email: String): User?
    fun selectUserByUsername(username: String): User?
    fun registerNewUser(user: User)
    fun updateUserInfo(user: User)
}