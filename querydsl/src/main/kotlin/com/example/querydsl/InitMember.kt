package com.example.querydsl

import com.example.querydsl.entity.Member
import com.example.querydsl.entity.Team
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Profile("local")
@Component
class InitMember {

    @Autowired
    lateinit var initMemberService: InitMemberService

    @PostConstruct
    fun init() {
        initMemberService.init()
    }

    companion object {
        @Component
        open class InitMemberService {
            @PersistenceContext
            lateinit var em: EntityManager

            @Transactional
            open fun init() {
                val teamA = Team("teamA")
                val teamB = Team("teamB")
                em.persist(teamA)
                em.persist(teamB)

                for (i in 0 until 100) {
                    val selectTeam = if (i % 2 == 0) teamA else teamB
                    em.persist(Member("member${i}", i, selectTeam))
                }
            }
        }
    }
}
