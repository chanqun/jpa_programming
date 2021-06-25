package study.datajpa.controller

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import study.datajpa.dto.MemberDto
import study.datajpa.entity.Member
import study.datajpa.repository.MemberRepository

@RestController
class MemberController(
    private val memberRepository: MemberRepository
) {

    @GetMapping("/members/{id}")
    fun findMember(@PathVariable("id") id: Long): String {
        val member = memberRepository.findById(id).get()
        return member.username
    }

    @GetMapping("/members2/{id}")
    fun findMember2(@PathVariable("id") member: Member): String {
        // 도메인 클래스 컨버터 - 리포지토리를 사용해서 엔티티르 찾음
        return member.username
    }

    @GetMapping("/members")
    fun list(pageable: Pageable): Page<MemberDto> {
        // members?page=0&size=3&sort=id,desc&sort=username,desc 도 사용 가능하다
        // !! 꼭 Dto로 반환하는 것이 좋다!
        return memberRepository.findAll(pageable).map {
            MemberDto(it.id!!, it.username, it.team?.name)
        }
    }

    //@PostConstruct
    fun init() {
        for (i in 1..100) {
            memberRepository.save(Member("chanqun${i}", 20))
        }
    }
}
