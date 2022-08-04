package com.ryouonritsu.inkbook_backend.dao

import com.ryouonritsu.inkbook_backend.entity.Documentation
import org.apache.ibatis.annotations.Mapper

@Mapper
interface DocumentationDao {
    fun new(documentation: Documentation)
    fun delete(doc_id: Long)
    fun update(documentation: Documentation)
    fun find(doc_id: Long): Documentation?
    fun findByProjectId(project_id: Int): List<Documentation>
    fun findByCreatorId(creator_id: Int): List<Documentation>
}