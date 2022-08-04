package com.ryouonritsu.inkbook_backend.entity

import javax.persistence.*

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

    @OneToMany(targetEntity = Documentation::class, cascade = [CascadeType.ALL])
    @JoinColumn(name = "doc_id")
    var favorite_documents = mutableListOf<Documentation>()

    @OneToMany(targetEntity = Documentation::class, cascade = [CascadeType.ALL])
    @JoinColumn(name = "doc_id")
    var recently_viewed_documents = mutableListOf<Documentation>()

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
