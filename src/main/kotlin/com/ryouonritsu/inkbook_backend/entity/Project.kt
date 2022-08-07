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
    var team_id: Long,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var project_id = 0
    var deprecated = false

    constructor() : this("", "", -1)
}