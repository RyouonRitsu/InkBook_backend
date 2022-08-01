package com.ryouonritsu.demo.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class User(
    var username: String,
    var password: String
) {
    constructor() : this("", "")

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var userId = 0L
}