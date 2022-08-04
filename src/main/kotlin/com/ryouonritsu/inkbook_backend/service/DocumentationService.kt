package com.ryouonritsu.inkbook_backend.service

import com.ryouonritsu.inkbook_backend.entity.Documentation

interface DocumentationService {
    operator fun plus(documentation: Documentation)
    operator fun minus(doc_id: Long)
    operator fun invoke(documentation: Documentation)
    operator fun get(doc_id: Long): Documentation?
    fun findByProjectId(project_id: Int): List<Documentation>
    fun findByCreatorId(creator_id: Int): List<Documentation>
}