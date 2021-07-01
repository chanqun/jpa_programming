package com.example.querydsl.entity

import javax.persistence.*

@Entity
class Member(
    var username: String? = null,
    var age: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    var team: Team? = null,

    @Id @GeneratedValue
    @Column(name = "member_id")
    var id: Long? = null
) {
    fun changeTeam(team: Team) {
        this.team = team
        team.members.add(this)
    }
}
