package com.ryouonritsu.inkbook_backend.service.impl

import com.ryouonritsu.inkbook_backend.dao.UserDao
import com.ryouonritsu.inkbook_backend.entity.User
import com.ryouonritsu.inkbook_backend.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserServiceImpl : UserService {
    @Autowired
    lateinit var userDao: UserDao

    override fun selectUserByUserId(userId: Long) = userDao.selectUserByUserId(userId)

    override fun selectUserByEmail(email: String) = userDao.selectUserByEmail(email)

    override fun selectUserByUsername(username: String) = userDao.selectUserByUsername(username)

    override fun registerNewUser(user: User) = userDao.registerNewUser(user)
}