package study.datajpa.repository

import org.springframework.beans.factory.annotation.Autowired
import study.datajpa.entity.Member
import javax.persistence.EntityManager

interface MemberRepositoryCustom {
    fun findMemberCustom(): List<Member>
}

class MemberRepositoryImpl @Autowired constructor(
    private val em: EntityManager
) : MemberRepositoryCustom {

    override fun findMemberCustom(): List<Member> {
        return em.createQuery("select m from Member m", Member::class.java)
            .resultList
    }
    
}
