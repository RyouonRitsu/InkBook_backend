package com.ryouonritsu.inkbook_backend.service

import com.ryouonritsu.inkbook_backend.entity.User

interface UserService {
    fun selectUserByUsername(username: String): User?
    fun registerNewUser(user: User)
}