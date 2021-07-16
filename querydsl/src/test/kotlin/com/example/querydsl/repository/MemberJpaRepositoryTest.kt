package com.example.querydsl.repository

import com.example.querydsl.entity.Member
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

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
}