package com.ryouonritsu.inkbook_backend.dao

import com.ryouonritsu.inkbook_backend.entity.UML
import org.apache.ibatis.annotations.Mapper

/**
 *
 * @author WuKunchao
 */
@Mapper
interface UMLDao {
    fun createNewUML(uml: UML)
    fun updateUML(uml_id: String, last_modified: String, xml: String)
    fun selectUMLByUMLId(uml_id: String): UML?
    fun searchUMLByProjectId(project_id: String): List<Map<String, Any>>?
    fun updateUMLInfo(uml_id: String, uml_name: String)
    fun deleteUMLByUMLId(uml_id: String)
}