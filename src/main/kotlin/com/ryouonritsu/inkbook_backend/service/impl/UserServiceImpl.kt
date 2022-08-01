package com.ryouonritsu.demo.service.impl

import com.ryouonritsu.demo.dao.UserDao
import com.ryouonritsu.demo.entity.User
import com.ryouonritsu.demo.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserServiceImpl : UserService {
    @Autowired
    lateinit var userDao: UserDao

    override fun selectUserByUsername(username: String) = userDao.selectUserByUsername(username)

    override fun registerNewUser(user: User) = userDao.registerNewUser(user)
}