package com.ryouonritsu.inkbook_backend.dao

import com.ryouonritsu.inkbook_backend.entity.User
import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserDao {
    fun selectUserByUserId(userId: Long): Map<String, String>?
    fun selectUserByEmail(email: String): Map<String, String>?
    fun selectUserByUsername(username: String): Map<String, String>?
    fun registerNewUser(user: User)
}