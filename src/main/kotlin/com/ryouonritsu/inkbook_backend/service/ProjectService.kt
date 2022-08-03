package com.ryouonritsu.inkbook_backend.service

import com.ryouonritsu.inkbook_backend.entity.Project

/**
 *
 * @author WuKunchao
 */
interface ProjectService {
    fun createNewProject(project: Project)
}