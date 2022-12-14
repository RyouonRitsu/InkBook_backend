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

    override fun get(user_id: Long) = userDao.selectUserByUserId(user_id)

    override fun selectUserByEmail(email: String) = userDao.selectUserByEmail(email)

    override fun selectUserByUsername(username: String) = userDao.selectUserByUsername(username)

    override fun plus(user: User) = userDao.registerNewUser(user)

    override fun invoke(user: User) = userDao.updateUserInfo(user)
}