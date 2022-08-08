package com.ryouonritsu.inkbook_backend.repository

import com.ryouonritsu.inkbook_backend.entity.Documentation
import com.ryouonritsu.inkbook_backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface DocumentationRepository : JpaRepository<Documentation, Long> {
    fun findByCreator(creator: User): List<Documentation>

    @Query("SELECT d FROM Documentation d WHERE d.project.project_id = ?1")
    fun findByPid(pId: Int): List<Documentation>

    @Query("SELECT d FROM Documentation d WHERE d.project.project_id = ?1 AND d.deprecated = ?2")
    fun findByPidAndDeprecated(pId: Int, deprecated: Boolean): List<Documentation>

    @Query("SELECT d FROM Documentation d WHERE d.project.project_id = ?2 AND (d.dname LIKE %?1% OR d.ddescription LIKE %?1% OR d.dcontent LIKE %?1%)")
    fun findByKeyword(keyword: String, projectId: Int): List<Documentation>
}