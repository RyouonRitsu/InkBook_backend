package com.ryouonritsu.inkbook_backend.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

/**
 *
 * @author WuKunchao
 */
@Entity
class UMLDao (
    var uml_name: String,
    var lastModified: String,
    var xml: String,
    var creator: String
    ) {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var uml_id = 0

        fun toDict() = mapOf(
            "uml_id" to uml_id,
            "uml_name" to uml_name,
            "lastModified" to lastModified,
            "xml" to xml,
            "creator" to creator
        )
    }