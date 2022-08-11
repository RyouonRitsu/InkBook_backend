package com.ryouonritsu.inkbook_backend.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

/**
 *
 * @author WuKunchao
 */
@Entity
class UML (
    var uml_name: String,
    var last_modified: String,
    @Column(columnDefinition = "LONGTEXT") var xml: String,
    var creator: String,
    var project_id: Int,
    ) {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var uml_id = 0

        fun toDict() = mapOf(
            "uml_id" to uml_id,
            "uml_name" to uml_name,
            "last_modified" to last_modified,
            "xml" to xml,
            "creator" to creator,
            "project_id" to project_id
        )
    }