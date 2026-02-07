package com.roulette.domain.user

import com.roulette.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true, length = 50)
    val nickname: String
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
