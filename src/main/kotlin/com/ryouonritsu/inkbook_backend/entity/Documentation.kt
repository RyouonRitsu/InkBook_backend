package com.ryouonritsu.inkbook_backend.entity

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Documentation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var doc_id: Long? = null
    var doc_name: String? = null
    var doc_description: String? = null
    var doc_content: String? = null
    var last_edit_time: String? = null
    var creator_username: String? = null
    var project_id: Int? = null

    constructor(
        doc_name: String,
        doc_description: String?,
        doc_content: String?,
        creator_username: String,
        project_id: Int
    ) {
        this.doc_name = doc_name
        this.doc_description = doc_description
        this.doc_content = doc_content
        this.last_edit_time =
            LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        this.creator_username = creator_username
        this.project_id = project_id
    }

    constructor()

    fun toDict() = mapOf(
        "doc_id" to doc_id,
        "doc_name" to doc_name,
        "doc_description" to doc_description,
        "doc_content" to doc_content,
        "last_edit_time" to last_edit_time,
        "creator_username" to creator_username,
        "project_id" to project_id
    )
}