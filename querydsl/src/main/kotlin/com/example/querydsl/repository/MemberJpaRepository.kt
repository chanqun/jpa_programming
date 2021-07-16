package com.example.querydsl.repository

import com.example.querydsl.entity.Member
import com.example.querydsl.entity.QMember.member
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
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


}