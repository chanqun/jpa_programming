package com.example.querydsl.repository

import com.example.querydsl.dto.MemberSearchCondition
import com.example.querydsl.entity.Member
import com.example.querydsl.entity.Team
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {
    @Autowired
    lateinit var em: EntityManager

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var memberJpaRepository: MemberJpaRepository

    @Test
    fun basicTest() {
        val member = Member("member1", 10)
        memberRepository.save(member)

        val result1 = memberRepository.findAll()
        assertThat(result1).containsExactly(member)

        val result2 = memberRepository.findByUsername("member1")
        assertThat(result2).containsExactly(member)
    }

    @Test
    fun basicQueryDslTest() {
        val member = Member("member1", 10)
        memberRepository.save(member)

        val result3 = memberJpaRepository.findAll()
        assertThat(result3).containsExactly(member)

        val result4 = memberJpaRepository.findByUsername("member1")
        assertThat(result4).containsExactly(member)
    }

    @Test
    fun searchTest() {
        val teamA = Team("teamA")
        val teamB = Team("teamB")

        em.persist(teamA)
        em.persist(teamB)

        val member1 = Member("member1", 10, teamA)
        val member2 = Member("member2", 20, teamA)

        val member3 = Member("member3", 30, teamB)
        val member4 = Member("member4", 40, teamB)

        em.persist(member1)
        em.persist(member2)
        em.persist(member3)
        em.persist(member4)

        em.flush()
        em.clear()

        val condition = MemberSearchCondition()
        //조건이 다 빠지는 경우에는 쿼리가 데이타를 다 가져온다. 동적쿼리를 할 때는 기본 쿼리를 두거나 limit 를 두는 것이 좋다.
        condition.ageGoe = 35
        condition.ageLoe = 40
        condition.teamName = "teamB"

        val result = memberJpaRepository.searchByBuilder(condition)

        assertThat(result).extracting("username").containsExactly("member4")
    }
}
