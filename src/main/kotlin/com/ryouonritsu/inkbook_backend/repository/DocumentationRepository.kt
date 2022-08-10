package com.ryouonritsu.inkbook_backend.repository

import com.ryouonritsu.inkbook_backend.entity.Documentation
import com.ryouonritsu.inkbook_backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
interface DocumentationRepository : JpaRepository<Documentation, Long> {
    fun findByCreatorOrderByLastedittimeDesc(creator: User): List<Documentation>

    @Query("SELECT d FROM Documentation d WHERE d.project.project_id = ?1 ORDER BY d.lastedittime DESC")
    fun findByPid(pId: Int): List<Documentation>

    @Query("SELECT d FROM Documentation d WHERE d.project.project_id = ?1 AND d.deprecated = ?2 ORDER BY d.lastedittime DESC")
    fun findByPidAndDeprecated(pId: Int, deprecated: Boolean): List<Documentation>

    @Query("SELECT d FROM Documentation d WHERE d.team.teamId = ?2 AND (d.dname LIKE %?1% OR d.ddescription LIKE %?1% OR d.dcontent LIKE %?1%) ORDER BY d.lastedittime DESC")
    fun findByKeywordAndTeamId(keyword: String, teamId: Int): List<Documentation>

    @Query("SELECT d FROM Documentation d WHERE d.project.project_id = ?2 AND (d.dname LIKE %?1% OR d.ddescription LIKE %?1% OR d.dcontent LIKE %?1%) ORDER BY d.lastedittime DESC")
    fun findByKeywordAndProjectId(keyword: String, projectId: Int): List<Documentation>

    @Transactional
    @Modifying
    @Query("DELETE FROM Documentation d WHERE d.did = ?1")
    override fun deleteById(id: Long)

    @Transactional
    @Modifying
    @Query("DELETE FROM Documentation d WHERE d = ?1")
    override fun delete(entity: Documentation)
}