-- V6: 회수 예정 포인트 채권 분리 모델 추가
-- 룰렛 취소 시 회수하지 못한 포인트를 "채권"으로 기록하고
-- 다음 포인트 지급 시 자동 차감하는 시스템 구현

-- 회수 예정 포인트 테이블 생성
CREATE TABLE IF NOT EXISTS pending_point_recovery (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    roulette_history_id BIGINT NOT NULL,
    amount_to_recover INT NOT NULL CHECK (amount_to_recover >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    cancelled_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    -- 외래 키 제약조건
    CONSTRAINT fk_recovery_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_recovery_roulette FOREIGN KEY (roulette_history_id) REFERENCES roulette_history(id),

    -- 상태 제약조건
    CONSTRAINT chk_recovery_status CHECK (status IN ('PENDING', 'COMPLETED'))
);

-- 인덱스 생성 (조회 최적화)
-- PENDING 상태의 회수 예정 포인트를 빠르게 조회하기 위한 복합 인덱스
CREATE INDEX idx_user_status ON pending_point_recovery(user_id, status);

-- PointType enum에 RECOVERY_DEDUCTION 추가
-- 기존 제약조건 삭제 (존재하는 경우)
ALTER TABLE point_ledger DROP CONSTRAINT IF EXISTS point_ledger_type_check;

-- 새 제약조건 추가 (RECOVERY_DEDUCTION 포함)
ALTER TABLE point_ledger ADD CONSTRAINT point_ledger_type_check
  CHECK (type IN ('EARN', 'REFUND', 'USED', 'RECLAIMED', 'RECOVERY_DEDUCTION'));

-- 커멘트 추가 (문서화)
COMMENT ON TABLE pending_point_recovery IS '회수 예정 포인트 (룰렛 취소 시 회수하지 못한 포인트를 다음 지급 시 자동 차감)';
COMMENT ON COLUMN pending_point_recovery.amount_to_recover IS '회수할 남은 금액 (차감되면 감소)';
COMMENT ON COLUMN pending_point_recovery.status IS '회수 상태: PENDING(대기), COMPLETED(완료)';
COMMENT ON COLUMN pending_point_recovery.cancelled_at IS '룰렛 취소 일시';
COMMENT ON COLUMN pending_point_recovery.completed_at IS '회수 완료 일시 (전액 회수 시)';
