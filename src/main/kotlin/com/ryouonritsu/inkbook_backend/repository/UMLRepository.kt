package com.ryouonritsu.inkbook_backend.repository

import com.ryouonritsu.inkbook_backend.entity.UML
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import javax.transaction.Transactional

interface UMLRepository : JpaRepository<UML, Int> {
    @Transactional
    @Modifying
    @Query("DELETE FROM UML u WHERE u.uml_id = ?1")
    override fun deleteById(id: Int)

    @Query(
        "SELECT u " +
                "FROM UML u " +
                "INNER JOIN Project p ON u.project_id = p.project_id " +
                "INNER JOIN Team t ON p.team_id = t.teamId " +
                "WHERE p.team_id = ?2 AND u.uml_name LIKE %?1%"
    )
    fun findByKeywordAndTeamId(keyword: String, teamId: Int): List<UML>

    @Query("SELECT u FROM UML u WHERE u.project_id = ?2 AND u.uml_name LIKE %?1%")
    fun findByKeywordAndProjectId(keyword: String, projectId: Int): List<UML>
}