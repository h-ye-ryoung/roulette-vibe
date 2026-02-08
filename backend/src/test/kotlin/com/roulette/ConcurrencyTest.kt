package com.roulette

import com.roulette.domain.admin.AdminService
import com.roulette.domain.budget.DailyBudgetRepository
import com.roulette.domain.order.OrderRepository
import com.roulette.domain.order.OrderService
import com.roulette.domain.point.PointLedger
import com.roulette.domain.point.PointLedgerRepository
import com.roulette.domain.point.PointType
import com.roulette.domain.product.Product
import com.roulette.domain.product.ProductRepository
import com.roulette.domain.roulette.RouletteHistoryRepository
import com.roulette.domain.roulette.RouletteService
import com.roulette.domain.user.User
import com.roulette.domain.user.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyTest {

    @Autowired
    private lateinit var rouletteService: RouletteService

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var adminService: AdminService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var rouletteHistoryRepository: RouletteHistoryRepository

    @Autowired
    private lateinit var dailyBudgetRepository: DailyBudgetRepository

    @Autowired
    private lateinit var pointLedgerRepository: PointLedgerRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @AfterEach
    fun cleanup() {
        // 테스트 데이터 정리
        orderRepository.deleteAll()
        pointLedgerRepository.deleteAll()
        rouletteHistoryRepository.deleteAll()
        dailyBudgetRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()
    }

    /**
     * T-1: 동일 유저 10개 동시 룰렛 요청 → 1건만 성공
     */
    @Test
    fun `T-1 동일 유저가 동시에 10번 룰렛 참여 시 1건만 성공해야 한다`() {
        // Given: 유저 생성
        val user = userRepository.save(User(nickname = "concurrent_user"))
        val userId = user.id

        // When: 10개의 스레드가 동시에 룰렛 참여 시도
        val threadCount = 10
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        repeat(threadCount) {
            executorService.submit {
                try {
                    latch.countDown()
                    latch.await() // 모든 스레드가 대기

                    rouletteService.spin(userId)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                }
            }
        }

        executorService.shutdown()
        while (!executorService.isTerminated) {
            Thread.sleep(100)
        }

        // Then: 1건만 성공, 9건 실패
        assertEquals(1, successCount.get(), "1건만 성공해야 함")
        assertEquals(9, failCount.get(), "9건은 실패해야 함")

        // DB 확인: 1건만 저장
        val histories = rouletteHistoryRepository.findAllByUserId(userId)
        assertEquals(1, histories.size, "DB에 1건만 저장되어야 함")
    }

    /**
     * T-2: 100명 동시 룰렛 (예산 100,000p) → 총 지급액 <= 100,000p
     */
    @Test
    fun `T-2 100명이 동시에 룰렛 참여 시 총 지급액이 예산을 초과하지 않아야 한다`() {
        // Given: 100명의 유저 생성
        val userCount = 100
        val users = (1..userCount).map { i ->
            userRepository.save(User(nickname = "user_$i"))
        }

        // When: 100명이 동시에 룰렛 참여
        val executorService = Executors.newFixedThreadPool(userCount)
        val latch = CountDownLatch(userCount)
        val successCount = AtomicInteger(0)

        users.forEach { user ->
            executorService.submit {
                try {
                    latch.countDown()
                    latch.await()

                    rouletteService.spin(user.id)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    // 예산 소진 시 실패 허용
                }
            }
        }

        executorService.shutdown()
        while (!executorService.isTerminated) {
            Thread.sleep(100)
        }

        // Then: 총 지급액이 100,000p 이하
        val histories = rouletteHistoryRepository.findAll()
        val totalIssued = histories.sumOf { it.amount }

        assertTrue(totalIssued <= 100000, "총 지급액($totalIssued)이 예산(100,000)을 초과하지 않아야 함")
        println("성공: ${successCount.get()}명, 총 지급액: ${totalIssued}p")
    }

    /**
     * T-3: 예산 500p 남은 상태에서 10명 동시 요청 → 최대 5명만 성공
     */
    @Test
    fun `T-3 예산이 500p 남은 상태에서 10명 동시 참여 시 최대 5명만 성공해야 한다`() {
        // Given: 예산을 직접 500p로 설정
        val today = java.time.LocalDate.now()
        val budget = com.roulette.domain.budget.DailyBudget(
            budgetDate = today,
            dailyLimit = 500,
            remaining = 500
        )
        dailyBudgetRepository.save(budget)

        // 테스트할 유저들 생성
        val testUsers = (1..10).map { i ->
            userRepository.save(User(nickname = "test_user_$i"))
        }

        // When: 10명이 동시에 룰렛 참여
        val executorService = Executors.newFixedThreadPool(10)
        val latch = CountDownLatch(10)
        val successCount = AtomicInteger(0)

        testUsers.forEach { user ->
            executorService.submit {
                try {
                    latch.countDown()
                    latch.await()

                    rouletteService.spin(user.id)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    // 예산 소진 실패 허용
                }
            }
        }

        executorService.shutdown()
        while (!executorService.isTerminated) {
            Thread.sleep(100)
        }

        // Then: 최대 5명만 성공 (100p ~ 1000p 범위이므로 500p로 최대 5명)
        assertTrue(successCount.get() <= 5, "최대 5명만 성공해야 함 (실제: ${successCount.get()})")
        println("성공: ${successCount.get()}명")
    }

    /**
     * T-4: 동일 상품(재고 1) 3명 동시 주문 → 1건만 성공
     */
    @Test
    fun `T-4 재고가 1개인 상품에 3명이 동시 주문 시 1건만 성공해야 한다`() {
        // Given: 3명의 유저 생성 및 포인트 지급
        val users = (1..3).map { i ->
            val user = userRepository.save(User(nickname = "buyer_$i"))
            // 각 유저에게 충분한 포인트 지급
            pointLedgerRepository.save(
                PointLedger(
                    userId = user.id,
                    amount = 1000,
                    balance = 1000,
                    type = PointType.EARN,
                    expiresAt = LocalDateTime.now().plusDays(30)
                )
            )
            user
        }

        // 재고 1인 상품 생성
        val product = productRepository.save(
            Product(
                name = "Limited Item",
                description = "재고 1",
                price = 500,
                stock = 1,
                isActive = true
            )
        )

        // When: 3명이 동시에 주문
        val executorService = Executors.newFixedThreadPool(3)
        val latch = CountDownLatch(3)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        users.forEach { user ->
            executorService.submit {
                try {
                    latch.countDown()
                    latch.await()

                    orderService.createOrder(user.id, product.id)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                }
            }
        }

        executorService.shutdown()
        while (!executorService.isTerminated) {
            Thread.sleep(100)
        }

        // Then: 1건만 성공
        assertEquals(1, successCount.get(), "1건만 성공해야 함")
        assertEquals(2, failCount.get(), "2건은 실패해야 함")

        // 재고 확인
        val updatedProduct = productRepository.findById(product.id).get()
        assertEquals(0, updatedProduct.stock, "재고가 0이 되어야 함")
    }

    /**
     * T-5: 만료 포인트로 주문 시도 → INSUFFICIENT_POINTS
     */
    @Test
    fun `T-5 만료된 포인트만 보유한 경우 주문 시 INSUFFICIENT_POINTS 예외가 발생해야 한다`() {
        // Given: 유저 생성 및 만료된 포인트 지급
        val user = userRepository.save(User(nickname = "expired_user"))
        pointLedgerRepository.save(
            PointLedger(
                userId = user.id,
                amount = 1000,
                balance = 1000,
                type = PointType.EARN,
                expiresAt = LocalDateTime.now().minusDays(1) // 이미 만료
            )
        )

        // 상품 생성
        val product = productRepository.save(
            Product(
                name = "Test Product",
                description = "테스트",
                price = 500,
                stock = 10,
                isActive = true
            )
        )

        // When & Then: 주문 시도 시 INSUFFICIENT_POINTS 예외
        val exception = assertThrows(Exception::class.java) {
            orderService.createOrder(user.id, product.id)
        }

        assertTrue(
            exception.message?.contains("INSUFFICIENT_POINTS") ?: false ||
            exception.javaClass.simpleName.contains("InsufficientPoints"),
            "INSUFFICIENT_POINTS 예외가 발생해야 함"
        )
    }

    /**
     * T-6: 주문 취소 후 포인트 잔액 복원 → balance 정확히 복원
     */
    @Test
    fun `T-6 주문 취소 시 사용된 포인트가 정확히 복원되어야 한다`() {
        // Given: 유저 생성 및 포인트 지급
        val user = userRepository.save(User(nickname = "refund_user"))
        val pointLedger = pointLedgerRepository.save(
            PointLedger(
                userId = user.id,
                amount = 1000,
                balance = 1000,
                type = PointType.EARN,
                expiresAt = LocalDateTime.now().plusDays(30)
            )
        )

        // 상품 생성 및 주문
        val product = productRepository.save(
            Product(
                name = "Refund Test",
                description = "환불 테스트",
                price = 600,
                stock = 10,
                isActive = true
            )
        )

        val orderResponse = orderService.createOrder(user.id, product.id)
        val orderId = orderResponse.orderId

        // 주문 후 잔액 확인
        val afterOrder = pointLedgerRepository.findById(pointLedger.id!!).get()
        assertEquals(400, afterOrder.balance, "주문 후 잔액은 400이어야 함")

        // When: 주문 취소 (AdminService 사용)
        adminService.cancelOrder(orderId)

        // Then: 잔액 복원 확인
        val afterCancel = pointLedgerRepository.findById(pointLedger.id!!).get()
        assertEquals(1000, afterCancel.balance, "취소 후 잔액이 1000으로 복원되어야 함")

        // 재고 복원 확인 (10 → 9 → 10)
        val updatedProduct = productRepository.findById(product.id).get()
        assertEquals(10, updatedProduct.stock, "재고가 10으로 복원되어야 함")
    }

    /**
     * T-7: 룰렛 취소 시 포인트 부분 사용 → 남은 포인트만 회수
     */
    @Test
    fun `T-7 룰렛 포인트 일부 사용 후 취소 시 남은 포인트만 회수되어야 한다`() {
        // Given: 유저 생성 및 룰렛 참여
        val user = userRepository.save(User(nickname = "cancel_user"))
        val spinResponse = rouletteService.spin(user.id)
        val historyId = spinResponse.historyId
        val originalAmount = spinResponse.amount

        // 포인트 일부 사용 (상품 주문)
        val product = productRepository.save(
            Product(
                name = "Partial Use",
                description = "부분 사용 테스트",
                price = originalAmount / 2, // 절반만 사용
                stock = 10,
                isActive = true
            )
        )
        orderService.createOrder(user.id, product.id)

        // 취소 전 잔액 확인
        val beforeCancel = pointLedgerRepository.findByRouletteHistoryId(historyId)!!
        val remainingBalance = beforeCancel.balance
        val usedAmount = originalAmount - remainingBalance

        // When: 룰렛 취소 (AdminService 사용)
        val cancelResponse = adminService.cancelRoulette(historyId)

        // Then: 이미 사용된 포인트는 유지, 남은 포인트만 회수
        assertTrue(usedAmount > 0, "일부 포인트가 사용되어야 함")
        assertTrue(remainingBalance > 0, "취소 전 남은 포인트가 있어야 함")

        assertEquals(remainingBalance, cancelResponse.reclaimedAmount, "회수된 금액이 남은 포인트와 일치해야 함")
        assertEquals(usedAmount, cancelResponse.alreadyUsedAmount, "이미 사용된 금액이 일치해야 함")

        val afterCancel = pointLedgerRepository.findById(beforeCancel.id!!).get()
        assertEquals(0, afterCancel.balance, "취소 후 balance는 0이어야 함")

        println("원래 금액: ${originalAmount}p, 사용: ${usedAmount}p, 회수: ${remainingBalance}p")
    }
}
