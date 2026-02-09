package com.roulette.domain.recovery

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * 회수 예정 포인트 Repository
 */
interface PendingPointRecoveryRepository : JpaRepository<PendingPointRecovery, Long> {

    /**
     * FIFO 순서로 회수 예정 포인트 조회 (비관적 락)
     *
     * 동시성 제어: PESSIMISTIC_WRITE 락으로 다른 트랜잭션의 접근 방지
     * FIFO 정렬: 생성일 순으로 정렬하여 먼저 생성된 회수 예정부터 차감
     *
     * @param userId 유저 ID
     * @return 생성일 순으로 정렬된 회수 예정 목록
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p FROM PendingPointRecovery p
        WHERE p.userId = :userId
        AND p.status = 'PENDING'
        ORDER BY p.createdAt ASC
    """)
    fun findPendingByUserIdForUpdate(@Param("userId") userId: Long): List<PendingPointRecovery>

    /**
     * 회수 예정 포인트 총액 조회
     *
     * 유저의 모든 PENDING 상태 회수 예정 포인트의 합계를 반환합니다.
     * 합계가 0보다 크면 사용 가능 잔액을 0으로 처리합니다 (불변 원칙).
     *
     * @param userId 유저 ID
     * @return 회수 예정 포인트 총액
     */
    @Query("""
        SELECT COALESCE(SUM(p.amountToRecover), 0)
        FROM PendingPointRecovery p
        WHERE p.userId = :userId
        AND p.status = 'PENDING'
    """)
    fun sumPendingAmount(@Param("userId") userId: Long): Int

    /**
     * 회수 예정 목록 조회 (조회용)
     *
     * UI 표시를 위한 회수 예정 목록을 생성일 순으로 조회합니다.
     * 락을 사용하지 않으므로 조회 전용으로만 사용해야 합니다.
     *
     * @param userId 유저 ID
     * @param status 회수 상태
     * @return 회수 예정 목록
     */
    fun findByUserIdAndStatusOrderByCreatedAtAsc(
        userId: Long,
        status: RecoveryStatus
    ): List<PendingPointRecovery>
}
