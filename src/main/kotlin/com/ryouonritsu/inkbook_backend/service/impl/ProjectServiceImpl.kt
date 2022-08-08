package com.ryouonritsu.inkbook_backend.service.impl

import com.ryouonritsu.inkbook_backend.dao.ProjectDao
import com.ryouonritsu.inkbook_backend.entity.Project
import com.ryouonritsu.inkbook_backend.service.ProjectService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 *
 * @author WuKunchao
 */
@Service
class ProjectServiceImpl : ProjectService {
    @Autowired
    lateinit var projectDao: ProjectDao

    override fun createNewProject(project: Project) = projectDao.createNewProject(project)

    override fun searchTeamIdByProjectId(project_id: String) = projectDao.searchTeamIdByProjectId(project_id)

    override fun deleteProject(project_id: String) = projectDao.deleteProject(project_id)

    override fun updateProject(
        project_id: String,
        project_name: String,
        project_info: String
    ) = projectDao.updateProject(project_id, project_name, project_info)

    override fun searchProjectByTeamId(team_id: String) = projectDao.searchProjectByTeamId(team_id)

    override fun searchProjectByProjectId(project_id: String) = projectDao.searchProjectByProjectId(project_id)

    override fun deprecateProjectByProjectId(
        project_id: String,
        deprecated: Boolean
    ) = projectDao.deprecateProjectByProjectId(project_id, deprecated)

    override fun updateProjectLastEditTime(
        project_id: String,
        time: String
    ) = projectDao.updateProjectLastEditTime(project_id, time)

    override fun searchProjectByKeyWord(
        team_id: String,
        keyword: String
    ) = projectDao.searchProjectByKeyWord(team_id, keyword)
}