package study.datajpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional
import study.datajpa.dto.MemberDto
import study.datajpa.entity.Member
import study.datajpa.entity.Team
import javax.persistence.EntityManager

@SpringBootTest
@Transactional
class MemberRepositoryTest {
    @Autowired
    lateinit var em: EntityManager

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var teamRepository: TeamRepository

    @Test
    fun testMember() {
        val teamA = Team("korea")
        val teamB = Team("korea2")

        val member = Member("chanqun", 29, teamA)
        val member2 = Member("chanqun1", 29, teamA)
        val member3 = Member("chanqun2", 29, teamB)
        val member4 = Member("chanqun3", 29, teamB)

        memberRepository.save(member)
        memberRepository.save(member2)
        memberRepository.save(member3)
        memberRepository.save(member4)

        val findMember = memberRepository.findById(member.id!!).get()

        assertThat(findMember.id).isEqualTo(member.id)
        assertThat(findMember.username).isEqualTo(member.username)
        assertThat(findMember).isEqualTo(member)
    }

    @Test
    fun `쿼리 메소드`() {
        val teamB = Team("korea2")
        teamRepository.save(teamB)

        val member3 = Member("chanqun2", 29, teamB)
        val member4 = Member("chanqun3", 24, teamB)
        memberRepository.save(member3)
        memberRepository.save(member4)

        val memberList = memberRepository.findByUsernameAndAgeGreaterThan("chanqun2", 10)

        memberList.forEach {
            println("${it.id}, ${it.username}")
        }
    }

    @Test
    fun findMemberDto() {
        val team = Team("korea")
        teamRepository.save(team)

        val member = Member("chanqun", 10, team)
        memberRepository.save(member)

        val findMemberDto = memberRepository.findMemberDto()

        findMemberDto.forEach { println(it) }
    }

    @Test
    fun `spring page`() {
        memberRepository.save(Member("chanqun", 29))
        memberRepository.save(Member("chanqun1", 29))
        memberRepository.save(Member("chanqun2", 29))
        memberRepository.save(Member("chanqun3", 29))
        memberRepository.save(Member("chanqun4", 29))

        val pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"))

        val page = memberRepository.findByAge(29, pageRequest)

        val map = page.map { member -> MemberDto(member.id!!, member.username) }

        map.forEach {
            println(it)
        }

        val content = page.content
        val totalElements = page.totalElements

        content.forEach {
            println(it.username)
        }

        assertThat(content.size).isEqualTo(3)
        assertThat(totalElements).isEqualTo(5)
        assertThat(page.number).isEqualTo(0)
        assertThat(page.totalPages).isEqualTo(2)
        assertThat(page.isFirst).isTrue
        assertThat(page.hasNext()).isTrue
    }

    @Test
    fun `Modifying`() {
        memberRepository.save(Member("chanqun", 21))
        memberRepository.save(Member("chanqun1", 22))
        memberRepository.save(Member("chanqun2", 19))
        memberRepository.save(Member("chanqun3", 18))
        memberRepository.save(Member("chanqun4", 30))

        val updateCount = memberRepository.bulkAgePlus(20)

        // bulk 연산 후에 em.clear()을 해서 영속성 컨텍스트를 날려주자
        // clear automatically 사용해도 됨

        assertThat(updateCount).isEqualTo(3)
    }

    @Test
    fun `findMember LAZY`() {
        val teamA = Team("teamA")
        val teamB = Team("teamB")

        teamRepository.save(teamA)
        teamRepository.save(teamB)

        val member = Member("chanqun", 29, teamA)
        val member1 = Member("toby", 40, teamB)
        memberRepository.save(member)
        memberRepository.save(member1)

        em.flush()
        em.clear()

        //select Member 1
        //when N + 1
        val findAll = memberRepository.findAll()
        // proxy만 가져온다.

        findAll.forEach {
            println(it.username)
            println(it.team!!.name)
        }
    }

    @Test
    fun queryHint() {
        val user = memberRepository.save(Member("chanqun", 29))
        em.flush()
        em.clear()

//        val findById = memberRepository.findById(user.id!!).get()
//        findById.username = "member2"
        //데이터를 바꾸기 위해 데이터를 가지고 있어야한다
        // 변경하지 않고 조회를 하려고 하더라도 내부적으로 가지고 있다. - 하이버네이트가 내부적으로 힌트를 제공해준다.

        val findMember = memberRepository.findReadOnlyByUsername("chanqun")

        println(findMember.username)
    }

    @Test
    fun lock() {
        val user = memberRepository.save(Member("chanqun", 29))

        //for update 가 붙음
        val memberList = memberRepository.findLockByUsername("chanqun")
    }

    @Test
    fun callCustom() {
        val result = memberRepository.findMemberCustom()
    }

    @Test
    fun JpaEventBaseEntity() {
        val member = Member("chanqun", 29)
        memberRepository.save(member)

        member.username = "sung"
        em.flush()
        em.clear()

        assertThat(member.createdDate).isNotNull
        assertThat(member.lastModifiedDate).isNotNull
        assertThat(member.createdDate).isNotEqualTo(member.lastModifiedDate)
    }

}
