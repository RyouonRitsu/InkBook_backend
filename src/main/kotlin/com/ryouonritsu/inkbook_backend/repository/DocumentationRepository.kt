package com.ryouonritsu.inkbook_backend.repository

import com.ryouonritsu.inkbook_backend.entity.Documentation
import com.ryouonritsu.inkbook_backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface DocumentationRepository : JpaRepository<Documentation, Long> {
    fun findByCreator(creator: User): List<Documentation>
    fun findByPid(pId: Int): List<Documentation>
}