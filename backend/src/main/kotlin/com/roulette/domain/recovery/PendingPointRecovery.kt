package com.roulette.domain.recovery

import com.roulette.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 회수 예정 포인트 상태
 */
enum class RecoveryStatus {
    PENDING,    // 회수 대기
    COMPLETED   // 회수 완료
}

/**
 * 회수 예정 포인트 (Pending Point Recovery)
 *
 * 룰렛 취소 시 현재 잔액보다 회수할 금액이 많을 경우,
 * 부족분을 "채권"으로 기록하여 다음 포인트 지급 시 자동 차감
 *
 * @property userId 대상 유저 ID
 * @property rouletteHistoryId 취소된 룰렛 히스토리 ID
 * @property amountToRecover 회수할 남은 금액 (차감되면 감소)
 * @property status 회수 상태 (PENDING/COMPLETED)
 * @property cancelledAt 룰렛 취소 일시
 * @property completedAt 회수 완료 일시 (전액 회수 시)
 */
@Entity
@Table(
    name = "pending_point_recovery",
    indexes = [
        Index(name = "idx_user_status", columnList = "user_id, status")
    ]
)
class PendingPointRecovery(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "roulette_history_id", nullable = false)
    val rouletteHistoryId: Long,

    @Column(name = "amount_to_recover", nullable = false)
    var amountToRecover: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: RecoveryStatus = RecoveryStatus.PENDING,

    @Column(name = "cancelled_at", nullable = false)
    val cancelledAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    /**
     * 회수 예정 포인트 차감
     *
     * 포인트 지급 시 회수 예정 금액을 차감합니다.
     * amountToRecover보다 큰 금액을 차감하려고 하면 최대 amountToRecover만 차감됩니다.
     * 전액 차감되면 상태를 COMPLETED로 변경하고 completedAt을 기록합니다.
     *
     * @param amount 차감할 금액
     * @return 실제 차감된 금액 (최대 amountToRecover)
     */
    fun deduct(amount: Int): Int {
        val actualDeduct = minOf(amount, amountToRecover)
        amountToRecover -= actualDeduct

        if (amountToRecover == 0) {
            status = RecoveryStatus.COMPLETED
            completedAt = LocalDateTime.now()
        }

        return actualDeduct
    }
}
