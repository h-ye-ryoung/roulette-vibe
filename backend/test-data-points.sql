-- 포인트 내역 페이지 테스트 데이터 생성
-- 닉네임: 시원한 메론

-- 1. 유저 생성 (이미 존재하면 무시)
INSERT INTO users (nickname)
VALUES ('시원한 메론')
ON CONFLICT (nickname) DO NOTHING;

-- 2. 유저 ID 확인
DO $$
DECLARE
    v_user_id BIGINT;
    v_today DATE := CURRENT_DATE;
    v_now TIMESTAMP := NOW();
BEGIN
    -- 유저 ID 조회
    SELECT id INTO v_user_id FROM users WHERE nickname = '시원한 메론';

    -- 3. 만료된 포인트 (5일 전에 만료)
    INSERT INTO point_ledger (
        user_id,
        amount,
        balance,
        type,
        issued_at,
        expires_at,
        roulette_history_id
    ) VALUES (
        v_user_id,
        300,
        0,  -- 만료되어 잔액 0
        'EARN',
        v_now - INTERVAL '35 days',  -- 35일 전 지급
        v_now - INTERVAL '5 days',   -- 5일 전 만료
        NULL
    );

    -- 4. 만료 예정 포인트 (5일 후 만료, D-5)
    INSERT INTO point_ledger (
        user_id,
        amount,
        balance,
        type,
        issued_at,
        expires_at,
        roulette_history_id
    ) VALUES (
        v_user_id,
        500,
        500,  -- 사용하지 않음
        'EARN',
        v_now - INTERVAL '25 days',  -- 25일 전 지급
        v_now + INTERVAL '5 days',   -- 5일 후 만료 (D-5)
        NULL
    );

    -- 5. 만료 예정 포인트 (3일 후 만료, D-3)
    INSERT INTO point_ledger (
        user_id,
        amount,
        balance,
        type,
        issued_at,
        expires_at,
        roulette_history_id
    ) VALUES (
        v_user_id,
        700,
        700,  -- 사용하지 않음
        'EARN',
        v_now - INTERVAL '27 days',  -- 27일 전 지급
        v_now + INTERVAL '3 days',   -- 3일 후 만료 (D-3)
        NULL
    );

    -- 6. 일반 유효 포인트 (20일 후 만료)
    INSERT INTO point_ledger (
        user_id,
        amount,
        balance,
        type,
        issued_at,
        expires_at,
        roulette_history_id
    ) VALUES (
        v_user_id,
        1000,
        1000,  -- 사용하지 않음
        'EARN',
        v_now - INTERVAL '10 days',  -- 10일 전 지급
        v_now + INTERVAL '20 days',  -- 20일 후 만료
        NULL
    );

    -- 7. 부분 사용 포인트 (800p 중 300p 사용, 500p 남음)
    INSERT INTO point_ledger (
        user_id,
        amount,
        balance,
        type,
        issued_at,
        expires_at,
        roulette_history_id
    ) VALUES (
        v_user_id,
        800,
        500,  -- 300p 사용, 500p 남음
        'EARN',
        v_now - INTERVAL '5 days',   -- 5일 전 지급
        v_now + INTERVAL '25 days',  -- 25일 후 만료
        NULL
    );

    -- 8. 사용 내역 (USED)
    INSERT INTO point_ledger (
        user_id,
        amount,
        balance,
        type,
        issued_at,
        expires_at,
        roulette_history_id
    ) VALUES (
        v_user_id,
        -300,  -- 사용은 음수
        0,     -- 사용 내역은 balance 0
        'USED',
        v_now - INTERVAL '3 days',   -- 3일 전 사용
        v_now + INTERVAL '27 days',  -- 만료일 (사용 내역이지만 필드는 필수)
        NULL
    );

    -- 9. 환불 내역 (REFUND) - 주문 취소로 포인트 복원
    INSERT INTO point_ledger (
        user_id,
        amount,
        balance,
        type,
        issued_at,
        expires_at,
        roulette_history_id
    ) VALUES (
        v_user_id,
        200,
        200,  -- 환불된 포인트
        'REFUND',
        v_now - INTERVAL '1 days',   -- 1일 전 환불
        v_now + INTERVAL '29 days',  -- 29일 후 만료
        NULL
    );

    RAISE NOTICE '테스트 데이터 생성 완료: 유저 ID = %', v_user_id;
    RAISE NOTICE '- 만료된 포인트: 300p (5일 전 만료)';
    RAISE NOTICE '- 만료 예정 포인트: 500p (D-5), 700p (D-3)';
    RAISE NOTICE '- 일반 유효 포인트: 1000p, 500p (부분 사용), 200p (환불)';
    RAISE NOTICE '- 사용 내역: -300p';
    RAISE NOTICE '총 포인트 잔액: 500 + 700 + 1000 + 500 + 200 = 2900p';
END $$;
