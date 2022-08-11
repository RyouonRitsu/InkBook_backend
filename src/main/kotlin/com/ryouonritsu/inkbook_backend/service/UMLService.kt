package com.ryouonritsu.inkbook_backend.service

import com.ryouonritsu.inkbook_backend.entity.UML

/**
 *
 * @author WuKunchao
 */
interface UMLService {
    fun createNewUML(uml: UML)
    fun updateUML(uml_id: String, last_modified: String, xml: String)
    fun selectUMLByUMLId(uml_id: String): UML?
    fun searchUMLByProjectId(project_id: String): List<Map<String, Any>>?
    fun updateUMLInfo(uml_id: String, uml_name: String)
    fun deleteUMLByUMLId(uml_id: String)
}