package com.example.querydsl

import com.example.querydsl.entity.Hello
import com.example.querydsl.entity.QHello
import com.querydsl.jpa.impl.JPAQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@SpringBootTest
@Transactional
class QuerydslApplicationTests {
    @Autowired
    lateinit var em: EntityManager

    @Test
    fun contextLoads() {
        val hello = Hello()
        em.persist(hello)

        val query = JPAQueryFactory(em)
        val qHello = QHello("h")

        val result = query
            .selectFrom(qHello)
            .fetchOne()
        
        assertThat(result).isEqualTo(hello)
        assertThat(result!!.id).isEqualTo(hello.id)
    }
}
