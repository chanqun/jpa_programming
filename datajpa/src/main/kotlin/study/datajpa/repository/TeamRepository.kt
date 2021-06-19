package study.datajpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import study.datajpa.entity.Team

interface TeamRepository : JpaRepository<Team, Long>