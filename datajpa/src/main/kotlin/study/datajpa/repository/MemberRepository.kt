package study.datajpa.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.*
import org.springframework.data.repository.query.Param
import study.datajpa.dto.MemberDto
import study.datajpa.entity.Member
import javax.persistence.LockModeType
import javax.persistence.QueryHint

interface MemberRepository : JpaRepository<Member, Long>, MemberRepositoryCustom {

    fun findByUsernameAndAgeGreaterThan(username: String, age: Int): List<Member>

    @Query("select m from Member m where m.username = :username and m.age = :age")
    fun findUser(@Param("username") username: String, @Param("age") age: Int)

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from  Member m join m.team t")
    fun findMemberDto(): List<MemberDto>

    fun findByAge(age: Int, pageable: Pageable): Page<Member>
    //totalCount 가 넘 많으면 slice가 나을 수 있음

    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    fun bulkAgePlus(@Param("age") age: Int): Int

    @Query("select m from Member m left join fetch m.team")
    fun findMemberFetchJoin(): List<Member>

    @QueryHints(value = [QueryHint(name = "org.hibernate.readOnly", value = "true")])
    fun findReadOnlyByUsername(name: String): Member

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findLockByUsername(username: String): List<Member>
}
