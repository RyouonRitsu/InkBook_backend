package com.ryouonritsu.inkbook_backend.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var user_id: Long? = null
    var email: String? = null
    var username: String? = null
    var password: String? = null
    var real_name: String? = null
    var avatar: String? = null

    constructor(email: String, username: String, password: String, real_name: String?, avatar: String?) {
        this.email = email
        this.username = username
        this.password = password
        this.real_name = real_name
        this.avatar = avatar
    }

    constructor()

    fun toDict() = mapOf(
        "user_id" to user_id,
        "email" to email,
        "username" to username,
        "password" to password,
        "real_name" to real_name,
        "avatar" to avatar
    )
}
