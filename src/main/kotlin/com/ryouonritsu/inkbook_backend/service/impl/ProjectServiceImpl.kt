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
}