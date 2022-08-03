package com.ryouonritsu.inkbook_backend.dao

import com.ryouonritsu.inkbook_backend.entity.User
import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserDao {
    fun selectUserByUserId(user_id: Long): User?
    fun selectUserByEmail(email: String): User?
    fun selectUserByUsername(username: String): User?
    fun registerNewUser(user: User)
    fun updateUserInfo(user: User)
}