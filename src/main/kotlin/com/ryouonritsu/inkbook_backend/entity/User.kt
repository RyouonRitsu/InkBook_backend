package com.ryouonritsu.inkbook_backend.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var uid: Long? = null
    var email: String? = null
    var username: String? = null
    var password: String? = null
    var realname: String? = null
    var avatar: String? = null

//    @OneToMany(targetEntity = Documentation::class, cascade = [CascadeType.ALL])
//    @JoinColumn(name = "doc_id")
//    var favorite_documents = mutableListOf<Documentation>()
//
//    @OneToMany(targetEntity = Documentation::class, cascade = [CascadeType.ALL])
//    @JoinColumn(name = "doc_id")
//    var recently_viewed_documents = mutableListOf<Documentation>()

    constructor(email: String, username: String, password: String, real_name: String?, avatar: String?) {
        this.email = email
        this.username = username
        this.password = password
        this.realname = real_name
        this.avatar = avatar
    }

    constructor()

    fun toDict() = mapOf(
        "user_id" to uid,
        "email" to email,
        "username" to username,
        "password" to password,
        "real_name" to realname,
        "avatar" to avatar
    )
}
