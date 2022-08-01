package com.ryouonritsu.inkbook_backend.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var userId: Long? = null
    var email: String? = null
    var username: String? = null
    var password: String? = null
    var realName: String? = null

    constructor(email: String?, username: String?, password: String?, realName: String?) {
        this.email = email
        this.username = username
        this.password = password
        this.realName = realName
    }

    constructor()
}