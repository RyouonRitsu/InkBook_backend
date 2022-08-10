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
    @Column(columnDefinition = "TEXT")
    var avatar: String? = null

    @ManyToMany(
        targetEntity = Documentation::class,
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH]
    )
    var favoritedocuments = mutableListOf<Documentation>()

    @OneToMany(targetEntity = User2Documentation::class, mappedBy = "user", cascade = [CascadeType.ALL])
    var user2documentations = mutableListOf<User2Documentation>()


    constructor(email: String, username: String, password: String, real_name: String?, avatar: String?) {
        this.email = email
        this.username = username
        this.password = password
        this.realname = real_name
        this.avatar = avatar
    }

    constructor()

    fun toDict(): Map<String, Any?> = mapOf(
        "user_id" to "$uid",
        "email" to email,
        "username" to username,
        "password" to password,
        "real_name" to realname,
        "avatar" to avatar
    )
}
