package com.example.querydsl

import com.example.querydsl.entity.Member
import com.example.querydsl.entity.QMember
import com.example.querydsl.entity.Team
import com.querydsl.jpa.impl.JPAQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@SpringBootTest
@Transactional
class QuerydslBasicTest {

    @Autowired
    lateinit var em: EntityManager

    @BeforeEach
    fun before() {
        val teamA = Team("teamA")
        val teamB = Team("teamB")

        em.persist(teamA)
        em.persist(teamB)

        val member1 = Member("chan", 29, teamA)
        val member2 = Member("member2", 29, teamA)

        val member3 = Member("member3", 29, teamB)
        val member4 = Member("member4", 29, teamB)

        em.persist(member1)
        em.persist(member2)
        em.persist(member3)
        em.persist(member4)

        em.flush()
        em.clear()
    }

    @Test
    fun startJPQL() {
        val queryString = "select m from Member m where m.username = :username"

        val findMember = em.createQuery(queryString, Member::class.java)
            .setParameter("username", "member2")
            .singleResult

        assertThat(findMember.username).isEqualTo("member2")
    }

    @Test
    fun startQuerydsl() {
        val jpaQueryFactory = JPAQueryFactory(em)
        val m = QMember("m")

        val member = jpaQueryFactory
            .select(m)
            .from(m)
            .where(m.username.eq("member2"))
            .fetchOne()

        assertThat(member!!.username).isEqualTo("member2")
    }
}
