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
class Team(
    var teamName: String,
    var teamInfo: String?,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var teamId = 0

    constructor() : this("", "")

    fun toDict() = mapOf(
        "team_id" to teamId,
        "team_name" to teamName,
        "team_info" to teamInfo
    )
}