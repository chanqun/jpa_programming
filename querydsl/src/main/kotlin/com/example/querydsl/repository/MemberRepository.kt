package com.example.querydsl.repository

import com.example.querydsl.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {

    fun findByUsername(username: String): List<Member>

}
