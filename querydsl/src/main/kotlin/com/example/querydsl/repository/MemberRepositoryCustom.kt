package com.example.querydsl.repository

import com.example.querydsl.dto.MemberSearchCondition
import com.example.querydsl.dto.MemberTeamDto
import com.example.querydsl.dto.QMemberTeamDto
import com.example.querydsl.entity.QMember
import com.example.querydsl.entity.QTeam
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import javax.annotation.PostConstruct
import javax.persistence.EntityManager

interface MemberRepositoryCustom {
    fun search(condition: MemberSearchCondition): List<MemberTeamDto>
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
                    QMember.member.id.`as`("memberId"),
                    QMember.member.username,
                    QMember.member.age,
                    QTeam.team.id.`as`("teamId"),
                    QTeam.team.name.`as`("teamName")
                )
            )
            .from(QMember.member)
            .leftJoin(QMember.member.team, QTeam.team)
            .where(
                usernameEq(condition.username),
                teamNameEq(condition.teamName),
                ageBetween(condition.ageGoe, condition.ageLoe)
            )
            .fetch()
    }

    private fun usernameEq(username: String?): BooleanExpression? {
        return username?.run {
            QMember.member.username.eq(username)
        }
    }

    private fun teamNameEq(teamName: String?): BooleanExpression? {
        return teamName?.run {
            QTeam.team.name.eq(teamName)
        }
    }

    private fun ageBetween(ageGoe: Int?, ageLoe: Int?): BooleanExpression? {
        return ageGoe(ageGoe)?.and(ageLoe(ageLoe))
    }

    private fun ageGoe(ageGoe: Int?): BooleanExpression? {
        return ageGoe?.run {
            QMember.member.age.goe(ageGoe)
        }
    }

    private fun ageLoe(ageLoe: Int?): BooleanExpression? {
        return ageLoe.run {
            QMember.member.age.loe(ageLoe)
        }
    }

}