package com.ryouonritsu.inkbook_backend.dao

import com.ryouonritsu.inkbook_backend.entity.Project
import org.apache.ibatis.annotations.Mapper

/**
 *
 * @author WuKunchao
 */
@Mapper
interface ProjectDao {
    fun createNewProject(project: Project)
    fun searchTeamIdByProjectId(project_id: String): String?
    fun deleteProject(project_id: String)
    fun updateProject(project_id: String, project_name: String, project_info: String)
    fun searchProjectByTeamId(team_id: String): List<Map<String, String>>?
    fun searchProjectByProjectId(project_id: String): Map<String, String>?
    fun deprecateProjectByProjectId(project_id: String, deprecated: Boolean)
    fun updateProjectLastEditTime(project_id: String, time: String)
}