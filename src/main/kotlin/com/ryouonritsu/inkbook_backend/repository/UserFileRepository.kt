package com.ryouonritsu.inkbook_backend.repository

import com.ryouonritsu.inkbook_backend.entity.UserFile
import org.springframework.data.jpa.repository.JpaRepository

interface UserFileRepository : JpaRepository<UserFile, Long> {
    fun findByUrl(url: String): UserFile?
    fun findByCreatorId(creatorId: Long): List<UserFile>
}