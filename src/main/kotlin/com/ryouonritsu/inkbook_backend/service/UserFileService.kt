package com.ryouonritsu.inkbook_backend.service

import com.ryouonritsu.inkbook_backend.entity.UserFile

/**
 *
 * @author WuKunchao
 */
interface UserFileService {
    fun saveFile(file: UserFile)
}