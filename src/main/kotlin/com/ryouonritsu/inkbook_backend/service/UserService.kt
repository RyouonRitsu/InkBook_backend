package com.ryouonritsu.demo.service

import com.ryouonritsu.demo.entity.User

interface UserService {
    fun selectUserByUsername(username: String): User?
    fun registerNewUser(user: User)
}