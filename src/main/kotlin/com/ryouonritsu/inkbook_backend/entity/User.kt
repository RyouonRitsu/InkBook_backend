package com.ryouonritsu.inkbook_backend.entity

import javax.persistence.*

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

    @ManyToMany(
        targetEntity = Documentation::class,
        fetch = FetchType.EAGER,
        cascade = [CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH]
    )
    var favoritedocuments = mutableListOf<Documentation>()

    @OneToMany(targetEntity = User2Documentation::class, mappedBy = "user")
    var user2documentations = mutableListOf<User2Documentation>()


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
