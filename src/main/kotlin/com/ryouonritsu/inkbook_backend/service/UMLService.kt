package com.ryouonritsu.inkbook_backend.service

import com.ryouonritsu.inkbook_backend.entity.Axure
import com.ryouonritsu.inkbook_backend.entity.UML

/**
 *
 * @author WuKunchao
 */
interface UMLService {
    fun createNewUML(uml: UML)
    fun updateUML(uml_id: String, last_modified: String, xml: String)
    fun selectUMLByUMLId(uml_id: String): UML?
}