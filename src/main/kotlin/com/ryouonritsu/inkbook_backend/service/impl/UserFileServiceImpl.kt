package com.ryouonritsu.inkbook_backend.service.impl

import com.ryouonritsu.inkbook_backend.dao.UserFileDao
import com.ryouonritsu.inkbook_backend.entity.UserFile
import com.ryouonritsu.inkbook_backend.service.UserFileService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 *
 * @author WuKunchao
 */
@Service
class UserFileServiceImpl : UserFileService {
    @Autowired
    lateinit var fileDao: UserFileDao

    override fun saveFile(file: UserFile) = fileDao.saveFile(file)
}