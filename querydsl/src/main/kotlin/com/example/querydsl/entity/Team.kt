package com.example.querydsl.entity

import javax.persistence.*

@Entity
class Team(
    var name: String,

    @OneToMany(mappedBy = "team")
    val members: MutableList<Member> = mutableListOf(),

    @Id @GeneratedValue
    @Column(name = "team_id")
    var id: Long? = null
)
