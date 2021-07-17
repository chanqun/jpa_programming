package com.example.querydsl.repository

import com.example.querydsl.dto.MemberSearchCondition
import com.example.querydsl.dto.MemberTeamDto
import com.example.querydsl.dto.QMemberTeamDto
import com.example.querydsl.entity.Member
import com.example.querydsl.entity.QMember.member
import com.example.querydsl.entity.QTeam.team
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import org.springframework.util.StringUtils.hasText
import javax.annotation.PostConstruct
import javax.persistence.EntityManager

@Repository
class MemberJpaRepository(
    var entityManager: EntityManager
) {

    // spring bean으로 등록해도 괜찮다.
    lateinit var queryFactory: JPAQueryFactory

    @PostConstruct
    fun init() {
        this.queryFactory = JPAQueryFactory(entityManager)
    }

    fun findAll(): List<Member> {
        return queryFactory
            .selectFrom(member)
            .fetch()
    }

    fun findByUsername(username: String): List<Member> {
        return queryFactory
            .selectFrom(member)
            .where(member.username.eq(username))
            .fetch()
    }

    fun searchByBuilder(condition: MemberSearchCondition): List<MemberTeamDto> {
        val builder = BooleanBuilder()
        if (hasText(condition.username)) {
            builder.and(member.username.eq(condition.username))
        }

        if (hasText(condition.teamName)) {
            builder.and(team.name.eq(condition.teamName))
        }

        if (condition.ageGoe != null) {
            builder.and(member.age.goe(condition.ageGoe))
        }

        if (condition.ageLoe != null) {
            builder.and(member.age.loe(condition.ageLoe))
        }

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
            .where(builder)
            .fetch()
    }

    fun search(condition: MemberSearchCondition): List<MemberTeamDto> {
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
