package com.roulette.domain.user

import com.roulette.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true, length = 50)
    val nickname: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val role: Role = Role.USER
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}

enum class Role {
    USER, ADMIN
}
