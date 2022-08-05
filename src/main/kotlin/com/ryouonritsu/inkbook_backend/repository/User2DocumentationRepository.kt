package com.ryouonritsu.inkbook_backend.repository

import com.ryouonritsu.inkbook_backend.entity.Documentation
import com.ryouonritsu.inkbook_backend.entity.User
import com.ryouonritsu.inkbook_backend.entity.User2Documentation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface User2DocumentationRepository : JpaRepository<User2Documentation, Long> {
    fun findByUserAndDoc(user: User, doc: Documentation): User2Documentation?

    @Query("SELECT u FROM User2Documentation u WHERE u.doc.did = :docId")
    fun findByDocId(@Param("docId") docId: Long): List<User2Documentation>
}