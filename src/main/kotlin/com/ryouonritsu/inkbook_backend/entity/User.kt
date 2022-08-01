package com.ryouonritsu.inkbook_backend.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class User(
    var email: String,
    var username: String,
    var password: String
) {
    constructor() : this("", "", "")
    constructor(email: String, username: String, password: String, realName: String) : this(email, username, password) {
        this.realName = realName
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var userId = 0L

    var realName: String? = null

    fun toDict() = mapOf(
        "userId" to userId,
        "email" to email,
        "username" to username,
        "password" to password,
        "realName" to realName
    )
}