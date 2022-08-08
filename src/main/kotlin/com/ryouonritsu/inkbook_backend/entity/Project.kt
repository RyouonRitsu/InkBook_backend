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
class Project(
    var project_name: String,
    var project_info: String?,
    var prj_create_time: String,
    var prj_last_edit_time: String,
    var team_id: Long,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var project_id = 0
    var deprecated = false

    constructor() : this("", "", "", "", -1)

    fun toDict(): Map<String, Any?> = mapOf(
        "project_id" to project_id,
        "project_name" to project_name,
        "project_info" to project_info,
        "prj_create_time" to prj_create_time,
        "prj_last_edit_time" to prj_last_edit_time,
        "team_id" to team_id,
        "prj_deprecated" to deprecated
    )
}