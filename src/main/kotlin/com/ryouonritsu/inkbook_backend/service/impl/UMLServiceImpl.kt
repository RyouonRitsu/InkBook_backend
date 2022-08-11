package com.ryouonritsu.inkbook_backend.service.impl

import com.ryouonritsu.inkbook_backend.dao.UMLDao
import com.ryouonritsu.inkbook_backend.entity.UML
import com.ryouonritsu.inkbook_backend.service.UMLService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 *
 * @author WuKunchao
 */
@Service
class UMLServiceImpl : UMLService {
    @Autowired
    lateinit var umlDao: UMLDao

    override fun createNewUML(uml: UML) = umlDao.createNewUML(uml)

    override fun selectUMLByUMLId(uml_id: String) = umlDao.selectUMLByUMLId(uml_id)

    override fun updateUML(
        uml_id: String,
        last_modified: String,
        xml: String
    ) = umlDao.updateUML(uml_id, last_modified, xml)

    override fun searchUMLByProjectId(project_id: String) = umlDao.searchUMLByProjectId(project_id)

    override fun updateUMLInfo(uml_id: String, uml_name: String) = umlDao.updateUMLInfo(uml_id, uml_name)

    override fun deleteUMLByUMLId(uml_id: String) = umlDao.deleteUMLByUMLId(uml_id)
}