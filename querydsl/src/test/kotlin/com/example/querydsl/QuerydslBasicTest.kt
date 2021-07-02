package com.example.querydsl

import com.example.querydsl.entity.Member
import com.example.querydsl.entity.QMember
import com.example.querydsl.entity.QMember.member
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

    lateinit var queryFactory: JPAQueryFactory

    @BeforeEach
    fun before() {
        queryFactory = JPAQueryFactory(em)

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
        // 같은 테이블을 join해야하는 경우만 선언해서 사용하면 된다.
        val m = QMember("m")

        val member = queryFactory
            .select(m)
            .from(m)
            .where(m.username.eq("member2"))
            .fetchOne()

        assertThat(member!!.username).isEqualTo("member2")
    }

    @Test
    fun `query dsl static으로 사용`() {
        val member = queryFactory
            .select(member)
            .from(member)
            .where(member.username.eq("member2"))
            .fetchOne()

        assertThat(member!!.username).isEqualTo("member2")
    }

    @Test
    fun search() {
        val findMember = queryFactory
            .selectFrom(member)
            .where(
                //vararg로 넘기기 때문에 ,으로 조건을 줘도 된다.
                //,을 사용하면 null을 무시하기 때문에 동적쿼리 작성에 기가막히다.
                member.username.eq("member2")
                    .and(member.age.eq(29))
            )
            .fetchOne()

        assertThat(findMember!!.username).isEqualTo("member2")
        assertThat(findMember.age).isEqualTo(29)
    }

    @Test
    fun `resultFetch`() {
        val fetch = queryFactory
            .selectFrom(member)
            .fetch()

        val fetchOne = queryFactory
            .selectFrom(member)
            .fetchOne()

        val fetchFirst = queryFactory
            .selectFrom(member)
            .fetchFirst()

        // total count와 모든 검색목록을 가져온다.
        val result = queryFactory
            .selectFrom(member)
            .fetchResults()

        val total = result.total
        val results = result.results

        // count만 가져옴
        queryFactory
            .selectFrom(member)
            .fetchCount()
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력 (nulls last)
     * nulls First도 있음
     */
    @Test
    fun sort() {
        em.persist(Member(null, 100))
        em.persist(Member("member5", 100))
        em.persist(Member("member6", 100))

        val orderBy = queryFactory
            .selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc(), member.username.asc().nullsLast())
            .fetch()

        assertThat(orderBy[0].username).isEqualTo("member5")
        assertThat(orderBy[1].username).isEqualTo("member6")
        assertThat(orderBy[2].username).isNull()
    }

    @Test
    fun paging1() {
        val result = queryFactory
            .selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1) // 몇 번째 부터 끊어서 하나를 스킵하겠다.
            .limit(2)
            .fetch()

        assertThat(result.size).isEqualTo(2)
    }

    @Test
    fun paging2() {
        // fetchResult total도 가져옴

        val fetchResults = queryFactory
            .selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetchResults()

        assertThat(fetchResults.total).isEqualTo(4)
        assertThat(fetchResults.results.size).isEqualTo(2)
    }
}
