package com.ryouonritsu.demo.dao

import com.ryouonritsu.demo.entity.User
import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserDao {
    fun selectUserByUsername(username: String): User?
    fun registerNewUser(user: User)
}