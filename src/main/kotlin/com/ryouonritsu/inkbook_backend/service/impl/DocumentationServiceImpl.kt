package com.ryouonritsu.inkbook_backend.service.impl

import com.ryouonritsu.inkbook_backend.dao.DocumentationDao
import com.ryouonritsu.inkbook_backend.entity.Documentation
import com.ryouonritsu.inkbook_backend.service.DocumentationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DocumentationServiceImpl : DocumentationService {
    @Autowired
    lateinit var docDao: DocumentationDao

    override fun plus(documentation: Documentation) = docDao.new(documentation)
    override fun minus(doc_id: Long)  = docDao.delete(doc_id)
    override fun invoke(documentation: Documentation) = docDao.update(documentation)
    override fun get(doc_id: Long) = docDao.find(doc_id)
    override fun findByProjectId(project_id: Int) = docDao.findByProjectId(project_id)
    override fun findByCreatorId(creator_id: Int) = docDao.findByCreatorId(creator_id)
}