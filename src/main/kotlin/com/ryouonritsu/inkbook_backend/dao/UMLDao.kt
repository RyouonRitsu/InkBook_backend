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
}