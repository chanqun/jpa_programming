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
        // ?????? ???????????? join???????????? ????????? ???????????? ???????????? ??????.
        val m = QMember("m")

        val member = queryFactory
            .select(m)
            .from(m)
            .where(m.username.eq("member2"))
            .fetchOne()

        assertThat(member!!.username).isEqualTo("member2")
    }

    @Test
    fun `query dsl static?????? ??????`() {
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
                //vararg??? ????????? ????????? ,?????? ????????? ?????? ??????.
                //,??? ???????????? null??? ???????????? ????????? ???????????? ????????? ???????????????.
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

        // total count??? ?????? ??????????????? ????????????.
        val result = queryFactory
            .selectFrom(member)
            .fetchResults()

        val total = result.total
        val results = result.results

        // count??? ?????????
        queryFactory
            .selectFrom(member)
            .fetchCount()
    }

    /**
     * ?????? ?????? ??????
     * 1. ?????? ?????? ????????????(desc)
     * 2. ?????? ?????? ????????????(asc)
     * ??? 2?????? ?????? ????????? ????????? ???????????? ?????? (nulls last)
     * nulls First??? ??????
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
            .offset(1) // ??? ?????? ?????? ????????? ????????? ???????????????.
            .limit(2)
            .fetch()

        assertThat(result.size).isEqualTo(2)
    }

    @Test
    fun paging2() {
        // fetchResult total??? ?????????

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
    fun `?????? ?????? aggregation`() {
        // tuple??? ?????????

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
     * ?????? ????????? ??? ?????? ?????? ????????? ?????????
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
            .join(member.team, team) //left join??? ??????
            .where(team.name.eq("teamA"))
            .fetch()

        assertThat(fetch)
            .extracting("username")
            .contains("chan", "member2")
    }

    /**
     * ???????????? ?????? ??????
     * ?????? ??????
     * ????????? ????????? ??? ????????? ?????? ?????? ??????
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
            // left join ?????? on?????? where ????????? ??????????????? ?????? ?????? ??????
            // ?????? ???????????? where ?????? ???????????? on??? ????????????.
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
        //kotlin ????????? ????????? ????????? ????????? memeber!! ????????? team??? ????????????

        assertThat(loaded).isTrue
    }

    /**
     * ????????? ?????? ?????? ????????? ??????
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
     * ????????? ?????? ????????? ??????
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
                    .`when`(28).then("????????????")
                    .`when`(29).then("????????????")
                    .otherwise("??????")
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
                    .`when`(member.age.between(0, 30)).then("0-30???")
                    .otherwise("??????")
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
        //enum??? ????????? ??? ?????? ????????????.
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
        //repository??? ???????????? ?????? ?????? ??????.
        //?????? controller????????? ????????? ????????? ????????? ?????? ????????? ????????? ?????? ??? ???????????? ?????? ????????????.
        //repository?????? ???????????? ???????????? dto??? ????????? ?????? ????????????.

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
        // ?????? JPA ????????? ????????? ?????????
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

    //field??? setter??? property ?????? ????????? ????????????
    //member.username.`as`("name"), as ?????? ?????? ???????????????
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
        // ????????? ????????? ????????? ??? ??????
        val fetch = queryFactory
            .select(
                Projections.constructor(
                    MemberDto::class.java,
                    member.username,
                    member.age
                    //member.id ????????? ????????? ?????? ?????? ???????????? ????????? ????????? ????????? ??? ??????
                )
            ).from(member)
            .fetch()

        fetch.forEach {
            println(it.toString())
        }
    }

    @Test
    fun findDtoQueryProjections() {
        // memberdto??? queryprojection?????? QMemberDto??? ?????????.
        // ????????? ????????? ?????? ?????? ?????? ??? ??????
        // ????????? memberdto ????????? querydsl??? ????????? ?????? ???

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
        //??????????????? setting ??? ??? ??????
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
    fun `dynamic Query where???`() {
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

    //?????? ?????? isServiceable, ????????? IN ??? ???????????? ????????????.
    private fun allEq(usernameParam: String?, ageParam: Int?): BooleanExpression? {
        return usernameEq(usernameParam)?.and(ageEq(ageParam))
    }

    @Test
    fun bulkUpdate() {
        //db?????? update ???????????? ????????? ??????????????? ???????????????.

        val count = queryFactory
            .update(member)
            .set(member.username, "?????????")
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
        //H2Dialect ??? ????????? ????????? ?????????, ???????????? ????????? ???
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
