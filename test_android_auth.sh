#!/bin/bash

API_BASE="https://roulette-backend-upmn.onrender.com"
NICKNAME="test_android_$(date +%s)"

echo "=== 안드로이드 인증 테스트 ==="
echo ""

# 1. 로그인
echo "1. 로그인 중..."
LOGIN_RESPONSE=$(curl -s -X POST "$API_BASE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"nickname\":\"$NICKNAME\"}" \
  -c cookies.txt)

echo "로그인 응답:"
echo "$LOGIN_RESPONSE" | jq '.'
echo ""

SESSION_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.data.sessionId')
echo "세션 ID: $SESSION_ID"
echo ""

# 2. 즉시 API 호출 (안드로이드 시뮬레이션)
echo "2. 즉시 룰렛 상태 조회 (X-Session-ID 헤더)..."
sleep 0.1  # 100ms 대기 (안드로이드와 유사)

STATUS_RESPONSE=$(curl -s -X GET "$API_BASE/api/user/roulette/status" \
  -H "Content-Type: application/json" \
  -H "X-Session-ID: $SESSION_ID")

echo "상태 조회 응답:"
echo "$STATUS_RESPONSE" | jq '.'
echo ""

if echo "$STATUS_RESPONSE" | grep -q '"success":true'; then
  echo "✅ 성공! 안드로이드 인증 정상 작동"
else
  echo "❌ 실패! 401 에러 발생"
  echo ""
  echo "3. 1초 대기 후 재시도..."
  sleep 1
  
  RETRY_RESPONSE=$(curl -s -X GET "$API_BASE/api/user/roulette/status" \
    -H "Content-Type: application/json" \
    -H "X-Session-ID: $SESSION_ID")
  
  echo "재시도 응답:"
  echo "$RETRY_RESPONSE" | jq '.'
  
  if echo "$RETRY_RESPONSE" | grep -q '"success":true'; then
    echo "✅ 재시도 성공! → 타이밍 이슈 확인됨"
  else
    echo "❌ 재시도도 실패! → DB 저장 문제"
  fi
fi

rm -f cookies.txt
