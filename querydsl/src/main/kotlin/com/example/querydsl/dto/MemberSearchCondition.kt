package com.example.querydsl.dto

data class MemberSearchCondition(

    //회원명, 팀명, 나이
    var username: String? = null,
    var teamName: String? = null,
    var ageGoe: Int? = null,
    var ageLoe: Int? = null
)