package com.roulette.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByNickname(nickname: String): Optional<User>
    fun existsByNickname(nickname: String): Boolean
}
