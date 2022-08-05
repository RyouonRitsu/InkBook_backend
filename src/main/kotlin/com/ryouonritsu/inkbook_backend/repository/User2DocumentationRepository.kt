package com.ryouonritsu.inkbook_backend.repository

import com.ryouonritsu.inkbook_backend.entity.Documentation
import com.ryouonritsu.inkbook_backend.entity.User
import com.ryouonritsu.inkbook_backend.entity.User2Documentation
import org.springframework.data.jpa.repository.JpaRepository

interface User2DocumentationRepository : JpaRepository<User2Documentation, Long> {
    fun findByUserAndDoc(user: User, doc: Documentation): User2Documentation?
}