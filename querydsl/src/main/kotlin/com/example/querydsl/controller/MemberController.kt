package com.example.querydsl.controller

import com.example.querydsl.dto.MemberSearchCondition
import com.example.querydsl.dto.MemberTeamDto
import com.example.querydsl.repository.MemberJpaRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MemberController {

    @Autowired
    lateinit var memberJpaRepository: MemberJpaRepository

    @GetMapping("/v1/members")
    fun funName(searchCondition: MemberSearchCondition): List<MemberTeamDto> {
        return memberJpaRepository.search(searchCondition)
    }
}
