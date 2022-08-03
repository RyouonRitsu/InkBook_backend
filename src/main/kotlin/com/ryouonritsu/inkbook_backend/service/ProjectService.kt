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
}