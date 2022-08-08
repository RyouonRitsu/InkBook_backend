package com.ryouonritsu.inkbook_backend.service

import com.ryouonritsu.inkbook_backend.entity.Project

/**
 *
 * @author WuKunchao
 */
interface ProjectService {
    fun createNewProject(project: Project)
    fun searchTeamIdByProjectId(project_id: String): String?
    fun deleteProject(project_id: String)
    fun updateProject(project_id: String, project_name: String, project_info: String)
    fun searchProjectByTeamId(team_id: String): List<Map<String, String>>?
    fun searchProjectByProjectId(project_id: String): Map<String, String>?
    fun deprecateProjectByProjectId(project_id: String, deprecated: Boolean)
    fun updateProjectLastEditTime(project_id: String, time: String)
}