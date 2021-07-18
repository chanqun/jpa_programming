package com.example.querydsl.controller

import com.example.querydsl.dto.MemberSearchCondition
import com.example.querydsl.dto.MemberTeamDto
import com.example.querydsl.repository.MemberJpaRepository
import com.example.querydsl.repository.MemberRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MemberController {

    @Autowired
    lateinit var memberJpaRepository: MemberJpaRepository

    @Autowired
    lateinit var memberRepository: MemberRepository

    @GetMapping("/v1/members")
    fun funName(searchCondition: MemberSearchCondition): List<MemberTeamDto> {
        return memberJpaRepository.search(searchCondition)
    }

    @GetMapping("/v2/members")
    fun searchMemberV2(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto> {
        return memberRepository.searchPageSimple(condition, pageable)
    }

    @GetMapping("/v3/members")
    fun searchMemberV3(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto> {
        return memberRepository.searchPageComplex(condition, pageable)
    }
}
