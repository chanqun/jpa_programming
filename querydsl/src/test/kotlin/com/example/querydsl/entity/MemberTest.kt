package com.example.querydsl.entity

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@SpringBootTest
@Transactional
internal class MemberTest{

    @PersistenceContext
    lateinit var em: EntityManager

    @Test
    fun testEntity() {
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

        val createQuery = em.createQuery("select m from Member m", Member::class.java).resultList

        createQuery.forEach {
            println("${it.username}, ${it.team!!.name}")
        }
    }
}
