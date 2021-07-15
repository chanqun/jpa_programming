package com.example.querydsl

import com.example.querydsl.dto.MemberDto
import com.example.querydsl.dto.QMemberDto
import com.example.querydsl.entity.Member
import com.example.querydsl.entity.QMember
import com.example.querydsl.entity.QMember.member
import com.example.querydsl.entity.QTeam.team
import com.example.querydsl.entity.Team
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions.select
import com.querydsl.jpa.impl.JPAQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.PersistenceUnit

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

        val member1 = Member("chan", 28, teamA)
        val member2 = Member("member2", 30, teamA)

        val member3 = Member("member3", 31, teamB)
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
                member.username.eq("member4")
                    .and(member.age.eq(29))
            )
            .fetchOne()

        assertThat(findMember!!.username).isEqualTo("member4")
        assertThat(findMember.age).isEqualTo(29)
    }

    @Test
    fun `resultFetch`() {
        val fetch = queryFactory
            .selectFrom(member)
            .fetch()
//
//        val fetchOne = queryFactory
//            .selectFrom(member)
//            .fetchOne()

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

    @Test
    fun `집합 함수 aggregation`() {
        // tuple로 출력함

        val result = queryFactory
            .select(
                member.count(),
                member.age.max(),
                member.age.sum(),
                member.age.avg(),
                member.age.min()
            )
            .from(member)
            .fetch()

        val tuple = result!![0]

        assertThat(tuple[member.count()]).isEqualTo(4)
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라
     */
    @Test
    fun group() {
        val fetch = queryFactory
            .select(
                team.name, member.age.avg()
            )
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
            .fetch()

        val teamA = fetch!![0]
        val teamB = fetch!![1]

        assertThat(teamA.get(team.name)).isEqualTo("teamA")
        assertThat(teamA.get(member.age.avg())).isEqualTo(29.0)

        assertThat(teamB.get(team.name)).isEqualTo("teamB")
        assertThat(teamB.get(member.age.avg())).isEqualTo(30.0)
    }

    @Test
    fun join() {
        val fetch = queryFactory
            .selectFrom(member)
            .join(member.team, team) //left join도 가능
            .where(team.name.eq("teamA"))
            .fetch()

        assertThat(fetch)
            .extracting("username")
            .contains("chan", "member2")
    }

    /**
     * 연관관계 없는 조인
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    fun theta_join() {
        em.persist(Member("teamA", 20))
        em.persist(Member("teamB", 20))

        val result = queryFactory
            .select(member)
            .from(member, team)
            .where(member.username.eq(team.name))
            .fetch()

        assertThat(result)
            .extracting("username")
            .contains("teamA", "teamB")
    }

    @Test
    fun join_on_filtering() {
        val tuple = queryFactory
            .select(member, team)
            .from(member)
            .join(member.team, team).on(team.name.eq("teamA"))
            // left join 하면 on으로 where 절에서 필터링하는 것과 성능 동일
            // 내부 조인이면 where 외부 조인이면 on을 사용한다.
            .fetch()

        tuple.forEach {
            println("tuple = ${it}")
        }
    }

    @Test
    fun join_on_no_relation() {
        em.persist(Member("teamA", 20))
        em.persist(Member("teamB", 20))

        val fetch = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(team).on(member.username.eq(team.name))
            .fetch()

        fetch.forEach {
            println("fetch = ${it}")
        }
    }

    @PersistenceUnit
    lateinit var emf: EntityManagerFactory

    @Test
    fun fetchJoinNo() {
        em.flush()
        em.clear()

        val member = queryFactory
            .selectFrom(member)
            .join(member.team, team)
            .fetchJoin()
            .where(member.username.eq("member2"))
            .fetchOne()

        val loaded = emf.persistenceUnitUtil.isLoaded(member!!.team)
        //kotlin 에서는 이렇게 확인이 힘들듯 memeber!! 하면서 team을 가져와서

        assertThat(loaded).isTrue
    }

    /**
     * 나이가 가장 많은 회원을 조회
     */
    @Test
    fun subQuery() {

        val memberSub = QMember("memberSub")

        val result = queryFactory
            .selectFrom(member)
            .where(
                member.age.eq(
                    select(memberSub.age.max())
                        .from(memberSub)
                )
            )
            .fetch()

        assertThat(result).extracting("age").containsExactly(31)
    }

    /**
     * 나이가 평균 이상인 회원
     */
    @Test
    fun subQuery2() {

        val memberSub = QMember("memberSub")

        val result = queryFactory
            .selectFrom(member)
            .where(
                member.age.goe(
                    select(memberSub.age.avg())
                        .from(memberSub)
                )
            )
            .fetch()

        assertThat(result).extracting("age").containsExactly(30, 31)
    }

    @Test
    fun subQueryIn() {

        val memberSub = QMember("memberSub")

        val result = queryFactory
            .selectFrom(member)
            .where(
                member.age.`in`(
                    select(memberSub.age)
                        .from(memberSub)
                        .where(memberSub.age.gt(10))
                )
            )
            .fetch()

        assertThat(result).extracting("age").containsExactly(28, 30, 31, 29)
    }

    @Test
    fun selectSubQuery() {
        val memberSub = QMember("memberSub")

        val fetch = queryFactory
            .select(
                member.username,
                select(memberSub.age.avg())
                    .from(memberSub)
            )
            .from(member)
            .fetch()

        fetch.forEach {
            print(it)
        }
    }

    @Test
    fun basicCase() {
        val fetch = queryFactory
            .select(
                member.age
                    .`when`(28).then("스물여덟")
                    .`when`(29).then("스물아홉")
                    .otherwise("기타")
            ).from(member)
            .fetch()

        fetch.forEach {
            println(it)
        }
    }

    @Test
    fun complexCase() {
        val fetch = queryFactory
            .select(
                CaseBuilder()
                    .`when`(member.age.between(0, 30)).then("0-30살")
                    .otherwise("기타")
            )
            .from(member)
            .fetch()

        fetch.forEach {
            println(it)
        }
    }

    @Test
    fun constant() {
        val fetch = queryFactory
            .select(member.username, Expressions.constant("A"))
            .from(member)
            .fetch()

        fetch.forEach {
            println(it)
        }
    }

    @Test
    fun concat() {
        //enum을 처리할 때 많이 사용한다.
        val fetch = queryFactory
            .select(member.username.concat("_").concat(member.age.stringValue()))
            .from(member)
            .where(member.username.eq("member2"))
            .fetch()

        fetch.forEach {
            println(it)
        }
    }

    @Test
    fun simpleProjection() {
        val fetch = queryFactory
            .select(member.username)
            .from(member)
            .fetch()

        fetch.forEach {
            println(it)
        }
    }

    @Test
    fun tupleProjection() {
        //repository를 넘어가는 것은 좋지 않다.
        //앞단 controller에서는 뒷단의 기술을 모르는 것이 나중에 기술이 바뀔 때 유연하게 대응 가능하다.
        //repository에서 정리하고 나갈때는 dto로 나가는 것을 추천한다.

        val fetch = queryFactory
            .select(member.username, member.age)
            .from(member)
            .fetch()

        fetch.forEach {
            println("${it.get(member.username)} = ${it.get(member.age)}")
        }
    }

    @Test
    fun findDtoJPQL() {
        // 순수 JPA 생성자 방식만 지원함
        val createQuery = em.createQuery(
            "select new com.example.querydsl.dto.MemberDto(m.username, m.age) from Member m",
            MemberDto::class.java
        ).resultList

        createQuery.forEach {
            println(it.toString())
        }
    }

    @Test
    @Disabled
    fun findDtoBySetter() {
        val fetch = queryFactory
            .select(
                Projections.bean(
                    MemberDto::class.java,
                    member.username,
                    member.age
                )
            ).from(member)
            .fetch()

        fetch.forEach {
            println(it.toString())
        }
    }

    //field와 setter는 property 명이 맞아야 들어간다
    //member.username.`as`("name"), as 하고 이름 바꿔줘야함
    //ExpressionUtils.as(JPAExpressions.select(memberSub.age.max()).from(memberSub))

    @Test
    @Disabled
    fun findDtofields() {
        val fetch = queryFactory
            .select(
                Projections.fields(
                    MemberDto::class.java,
                    member.username,
                    member.age
                )
            ).from(member)
            .fetch()

        fetch.forEach {
            println(it.toString())
        }
    }

    @Test
    fun findDtoConstructor() {
        // 런타임 오류가 발생할 수 있음
        val fetch = queryFactory
            .select(
                Projections.constructor(
                    MemberDto::class.java,
                    member.username,
                    member.age
                    //member.id 여기서 추가로 다른 것이 들어가서 런타임 오류가 발생할 수 있음
                )
            ).from(member)
            .fetch()

        fetch.forEach {
            println(it.toString())
        }
    }

    @Test
    fun findDtoQueryProjections() {
        // memberdto에 queryprojection달면 QMemberDto도 생긴다.
        // 컴파일 오류로 많은 것을 잡을 수 있음
        // 하지만 memberdto 자체가 querydsl의 영향을 받게 됨

        val fetch = queryFactory
            .select(
                QMemberDto(
                    member.username,
                    member.age
                )
            ).from(member)
            .fetch()

        fetch.forEach {
            println(it.toString())
        }
    }

    @Test
    fun dynamicQuery_BooleanBuilder() {
        val usernameParam = "member2"
        val ageParam = 30

        val result = searchMember1(usernameParam, ageParam)

        assertThat(result.size).isEqualTo(1)
    }

    private fun searchMember1(usernameParam: String?, ageParam: Int?): List<Member> {
        //초기값으로 setting 할 수 있음
        val builder = BooleanBuilder()

        if (usernameParam != null) {
            builder.and(member.username.eq(usernameParam))
        }

        if (ageParam != null) {
            builder.and(member.age.eq(ageParam))
        }

        return queryFactory
            .selectFrom(member)
            .where(builder)
            .fetch()
    }

    @Test
    fun `dynamic Query where문`() {
        val usernameParam = "member2"
        val ageParam = 30

        val result = searchMember2(usernameParam, ageParam)

        assertThat(result.size).isEqualTo(1)
    }

    private fun searchMember2(usernameParam: String?, ageParam: Int?): List<Member> {
//        return queryFactory
//            .selectFrom(member)
//            .where(usernameEq(usernameParam), ageEq(ageParam))
//            .fetch()
        return queryFactory
            .selectFrom(member)
            .where(allEq(usernameParam, ageParam))
            .fetch()
    }

    private fun ageEq(ageParam: Int?): BooleanExpression? {
        return ageParam?.run { member.age.eq(ageParam) }
    }

    private fun usernameEq(usernameParam: String?): BooleanExpression? {
        if (usernameParam == null) {
            return null
        }
        return member.username.eq(usernameParam)
    }

    //광고 상태 isServiceable, 날짜가 IN 등 재사용이 가능하다.
    private fun allEq(usernameParam: String?, ageParam: Int?): BooleanExpression? {
        return usernameEq(usernameParam)?.and(ageEq(ageParam))
    }

    @Test
    fun bulkUpdate() {
        //db에는 update 되었지만 영속성 컨텍스트는 그대로이다.

        val count = queryFactory
            .update(member)
            .set(member.username, "비회원")
            .where(member.age.gt(28))
            .execute()

        println(count)
        em.flush()
        em.clear()

        val fetch = queryFactory
            .selectFrom(member)
            .fetch()

        fetch.forEach {
            println(it.username)
        }
    }

    @Test
    fun bulkAdd() {
        val count = queryFactory
            .update(member)
            .set(member.age, member.age.add(1))
            .execute()
    }

    @Test
    fun bulkDelete() {
        queryFactory
            .delete(member)
            .where(member.age.gt(18))
            .execute()
    }

    @Test @Disabled
    fun `SQL function`() {
        //H2Dialect 에 등록된 함수를 써야함, 등록해서 쓰거나 함
        val fetch = queryFactory
            .select(
                Expressions.stringTemplate("function('replace', {0}, {1}, {2})", member.username, "member", "M")
            ).from(member)
            .fetch()

        fetch.forEach {
            println(it)
        }
    }

    @Test
    fun sqlFunction2() {
        val fetch = queryFactory
            .select(member.username)
            .from(member)
//            .where(member.username.eq(
//                Expressions.stringTemplate("function('lower', {0})", member.username
//                )))
            .where(member.username.eq(member.username.lower()))
            .fetch()

        fetch.forEach {
            println(it)
        }
    }
}
