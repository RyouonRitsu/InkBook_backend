package com.ryouonritsu.inkbook_backend.repository

import com.ryouonritsu.inkbook_backend.entity.DocumentationDict
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import javax.transaction.Transactional

interface DocumentationDictRepository : JpaRepository<DocumentationDict, Long> {
    fun findByNameAndTid(name: String, tid: Int): DocumentationDict?

    @Transactional
    @Modifying
    @Query("DELETE FROM DocumentationDict d WHERE d = ?1")
    override fun delete(documentationDict: DocumentationDict)

    @Transactional
    @Modifying
    @Query("DELETE FROM DocumentationDict d WHERE d.id = ?1")
    override fun deleteById(dictId: Long)
}