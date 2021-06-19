package study.datajpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
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

}