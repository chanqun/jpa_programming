package study.datajpa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.*

@EnableJpaAuditing
@SpringBootApplication
class DatajpaApplication {
    @Bean
    fun auditorProvider() : AuditorAware<String> {
        return AuditorAware {
            // 이 부분에서 세션을 가져오거나 한다.
            Optional.of(UUID.randomUUID().toString())
        }
    }
}

fun main(args: Array<String>) {
    runApplication<DatajpaApplication>(*args)
}

