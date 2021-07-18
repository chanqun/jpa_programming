package com.example.querydsl.repository

import com.example.querydsl.dto.MemberSearchCondition
import com.example.querydsl.dto.MemberTeamDto
import com.example.querydsl.dto.QMemberTeamDto
import com.example.querydsl.entity.Member
import com.example.querydsl.entity.QMember.member
import com.example.querydsl.entity.QTeam.team
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import javax.annotation.PostConstruct
import javax.persistence.EntityManager

interface MemberRepositoryCustom {

    fun search(condition: MemberSearchCondition): List<MemberTeamDto>
    fun searchPageSimple(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto>
    fun searchPageComplex(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto>

}

// 쿼리가 너무 복잡하면 따로 repository 로 빼는 것도 괜찮다.
class MemberRepositoryImpl(
    private var entityManager: EntityManager
) : MemberRepositoryCustom {

//    @Autowired
//    lateinit var entityManager: EntityManager

    private lateinit var queryFactory: JPAQueryFactory

    @PostConstruct
    fun init() {
        this.queryFactory = JPAQueryFactory(entityManager)
    }

    override fun search(condition: MemberSearchCondition): List<MemberTeamDto> {
        return queryFactory
            .select(
                QMemberTeamDto(
                    member.id.`as`("memberId"),
                    member.username,
                    member.age,
                    team.id.`as`("teamId"),
                    team.name.`as`("teamName")
                )
            )
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.username),
                teamNameEq(condition.teamName),
                ageBetween(condition.ageGoe, condition.ageLoe)
            )
            .fetch()
    }

    override fun searchPageSimple(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto> {
        val results = queryFactory
            .select(
                QMemberTeamDto(
                    member.id.`as`("memberId"),
                    member.username,
                    member.age,
                    team.id.`as`("teamId"),
                    team.name.`as`("teamName")
                )
            )
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.username),
                teamNameEq(condition.teamName),
                ageBetween(condition.ageGoe, condition.ageLoe)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetchResults()

        val content = results.results
        val total = results.total

        return PageImpl(content, pageable, total)
    }

    override fun searchPageComplex(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto> {
        val content = queryFactory
            .select(
                QMemberTeamDto(
                    member.id.`as`("memberId"),
                    member.username,
                    member.age,
                    team.id.`as`("teamId"),
                    team.name.`as`("teamName")
                )
            )
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.username),
                teamNameEq(condition.teamName),
                ageBetween(condition.ageGoe, condition.ageLoe)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        var countQuery: JPAQuery<Member> = queryFactory
            .select(member)
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.username),
                teamNameEq(condition.teamName),
                ageBetween(condition.ageGoe, condition.ageLoe)
            )

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount)
        //return PageImpl(content, pageable, total)
    }

    private fun usernameEq(username: String?): BooleanExpression? {
        return username?.run {
            member.username.eq(username)
        }
    }

    private fun teamNameEq(teamName: String?): BooleanExpression? {
        return teamName?.run {
            team.name.eq(teamName)
        }
    }

    private fun ageBetween(ageGoe: Int?, ageLoe: Int?): BooleanExpression? {
        return ageGoe(ageGoe)?.and(ageLoe(ageLoe))
    }

    private fun ageGoe(ageGoe: Int?): BooleanExpression? {
        return ageGoe?.run {
            member.age.goe(ageGoe)
        }
    }

    private fun ageLoe(ageLoe: Int?): BooleanExpression? {
        return ageLoe.run {
            member.age.loe(ageLoe)
        }
    }

}
