package com.ryouonritsu.inkbook_backend.dao

import com.ryouonritsu.inkbook_backend.entity.User
import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserDao {
    fun selectUserByUsername(username: String): User?
    fun registerNewUser(user: User)
}