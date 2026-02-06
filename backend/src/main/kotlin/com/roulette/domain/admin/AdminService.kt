package com.roulette.domain.admin

import com.roulette.common.*
import com.roulette.domain.admin.dto.*
import com.roulette.domain.order.OrderStatus
import com.roulette.domain.budget.DailyBudget
import com.roulette.domain.budget.DailyBudgetRepository
import com.roulette.domain.order.OrderPointUsageRepository
import com.roulette.domain.order.OrderRepository
import com.roulette.domain.point.PointLedgerRepository
import com.roulette.domain.product.Product
import com.roulette.domain.product.ProductRepository
import com.roulette.domain.product.dto.ProductDetailResponse
import com.roulette.domain.product.dto.ProductItem
import com.roulette.domain.product.dto.ProductListResponse
import com.roulette.domain.roulette.RouletteHistoryRepository
import com.roulette.domain.user.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Service
class AdminService(
    private val dailyBudgetRepository: DailyBudgetRepository,
    private val rouletteHistoryRepository: RouletteHistoryRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val orderPointUsageRepository: OrderPointUsageRepository,
    private val pointLedgerRepository: PointLedgerRepository,
    private val userRepository: UserRepository,
    @Value("\${app.daily-budget-default:100000}") private val defaultDailyBudget: Int
) {

    private val kstZone = ZoneId.of("Asia/Seoul")

    @Transactional(readOnly = true)
    fun getDashboard(): DashboardResponse {
        val today = LocalDate.now(kstZone)
        val budget = dailyBudgetRepository.findByBudgetDate(today)

        return if (budget != null) {
            val participantCount = rouletteHistoryRepository.countBySpinDate(today)
            DashboardResponse(
                budgetDate = budget.budgetDate,
                dailyLimit = budget.dailyLimit,
                remaining = budget.remaining,
                usedAmount = budget.dailyLimit - budget.remaining,
                participantCount = participantCount.toInt()
            )
        } else {
            DashboardResponse(
                budgetDate = today,
                dailyLimit = defaultDailyBudget,
                remaining = defaultDailyBudget,
                usedAmount = 0,
                participantCount = 0
            )
        }
    }

    @Transactional(readOnly = true)
    fun getBudget(): BudgetResponse {
        val today = LocalDate.now(kstZone)
        val budget = dailyBudgetRepository.findByBudgetDate(today)

        return if (budget != null) {
            BudgetResponse(
                budgetDate = budget.budgetDate,
                dailyLimit = budget.dailyLimit,
                remaining = budget.remaining
            )
        } else {
            BudgetResponse(
                budgetDate = today,
                dailyLimit = defaultDailyBudget,
                remaining = defaultDailyBudget
            )
        }
    }

    @Transactional
    fun updateBudget(request: UpdateBudgetRequest): UpdateBudgetResponse {
        val today = LocalDate.now(kstZone)
        val tomorrow = today.plusDays(1)

        // 다음 날부터 적용: 내일 예산 레코드 생성 또는 업데이트
        val existingBudget = dailyBudgetRepository.findByBudgetDate(tomorrow)

        if (existingBudget != null) {
            // 이미 존재하면 업데이트 (DailyBudget은 data class가 아니므로 직접 수정 후 저장)
            val updated = DailyBudget(
                budgetDate = existingBudget.budgetDate,
                dailyLimit = request.dailyLimit,
                remaining = request.dailyLimit
            )
            dailyBudgetRepository.delete(existingBudget)
            dailyBudgetRepository.save(updated)
        } else {
            // 없으면 새로 생성
            val newBudget = DailyBudget(
                budgetDate = tomorrow,
                dailyLimit = request.dailyLimit,
                remaining = request.dailyLimit
            )
            dailyBudgetRepository.save(newBudget)
        }

        return UpdateBudgetResponse(
            dailyLimit = request.dailyLimit,
            effectiveFrom = tomorrow
        )
    }

    // ===== 상품 CRUD =====

    /**
     * 전체 상품 목록 조회 (ACTIVE + INACTIVE 모두)
     */
    @Transactional(readOnly = true)
    fun getProducts(): ProductListResponse {
        val products = productRepository.findAll()
        val productItems = products.map { ProductItem.from(it) }
        return ProductListResponse(productItems)
    }

    /**
     * 상품 생성
     */
    @Transactional
    fun createProduct(request: CreateProductRequest): ProductDetailResponse {
        val product = Product(
            name = request.name,
            description = request.description,
            price = request.price,
            stock = request.stock,
            isActive = true
        )
        val saved = productRepository.save(product)
        return ProductDetailResponse.from(saved)
    }

    /**
     * 상품 수정
     */
    @Transactional
    fun updateProduct(productId: Long, request: UpdateProductRequest): ProductDetailResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException() }

        product.name = request.name
        product.description = request.description
        product.price = request.price
        product.stock = request.stock
        product.isActive = request.isActive

        val updated = productRepository.save(product)
        return ProductDetailResponse.from(updated)
    }

    /**
     * 상품 삭제
     * - 주문 내역이 있으면 삭제 불가 (PRODUCT_HAS_ORDERS)
     */
    @Transactional
    fun deleteProduct(productId: Long): DeleteProductResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException() }

        // 주문 내역 체크
        val orders = orderRepository.findAllByProductId(productId)
        if (orders.isNotEmpty()) {
            throw ProductHasOrdersException()
        }

        productRepository.delete(product)

        return DeleteProductResponse(
            message = "Product deleted successfully"
        )
    }

    // ===== 주문 관리 =====

    /**
     * 전체 주문 내역 조회 (페이지네이션, 필터링)
     */
    @Transactional(readOnly = true)
    fun getOrders(page: Int, size: Int, status: OrderStatus?): AdminOrderListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))

        // 전체 조회 후 필터링 (간단한 구현)
        val orderPage = orderRepository.findAll(pageable)

        val filteredOrders = if (status != null) {
            orderPage.content.filter { it.status == status }
        } else {
            orderPage.content
        }

        // User 및 Product 정보 조회
        val userIds = filteredOrders.map { it.userId }.toSet()
        val productIds = filteredOrders.map { it.productId }.toSet()

        val users = userRepository.findAllById(userIds).associateBy { it.id }
        val products = productRepository.findAllById(productIds).associateBy { it.id }

        val orderItems = filteredOrders.mapNotNull { order ->
            val user = users[order.userId] ?: return@mapNotNull null
            val product = products[order.productId] ?: return@mapNotNull null

            AdminOrderItem.from(order, user.nickname, product.name)
        }

        return AdminOrderListResponse(
            orders = orderItems,
            page = orderPage.number,
            size = orderPage.size,
            totalElements = orderPage.totalElements
        )
    }

    /**
     * 주문 취소
     * - OrderPointUsage 기반으로 포인트 복원
     * - Product 재고 복원
     */
    @Transactional
    fun cancelOrder(orderId: Long): CancelOrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException() }

        // 이미 취소된 주문
        if (order.isCancelled()) {
            throw OrderAlreadyCancelledException()
        }

        // 주문 상태 변경
        order.cancel()

        // OrderPointUsage 조회 (사용된 포인트 내역)
        val usages = orderPointUsageRepository.findAllByOrderId(orderId)

        // PointLedger balance 복원 (PDP-4: 원래 포인트 복원)
        var totalRefunded = 0
        for (usage in usages) {
            val pointLedger = pointLedgerRepository.findById(usage.pointLedgerId)
                .orElseThrow { IllegalStateException("PointLedger not found") }

            pointLedger.restore(usage.usedAmount)
            totalRefunded += usage.usedAmount
        }

        // Product 재고 복원
        productRepository.incrementStock(order.productId)

        // User 조회 (응답용)
        val user = userRepository.findById(order.userId)
            .orElseThrow { IllegalStateException("User not found") }

        return CancelOrderResponse(
            orderId = orderId,
            userId = order.userId,
            userName = user.nickname,
            refundedAmount = totalRefunded,
            message = "Order cancelled and ${totalRefunded}p refunded"
        )
    }

    // ===== 룰렛 관리 =====

    /**
     * 룰렛 참여 내역 조회 (페이지네이션, 날짜 필터)
     */
    @Transactional(readOnly = true)
    fun getRouletteHistory(page: Int, size: Int, date: LocalDate?): AdminRouletteHistoryResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))

        // 전체 조회 후 필터링
        val historyPage = rouletteHistoryRepository.findAll(pageable)

        val filteredHistories = if (date != null) {
            historyPage.content.filter { it.spinDate == date }
        } else {
            historyPage.content
        }

        // User 정보 조회
        val userIds = filteredHistories.map { it.userId }.toSet()
        val users = userRepository.findAllById(userIds).associateBy { it.id }

        val historyItems = filteredHistories.mapNotNull { history ->
            val user = users[history.userId] ?: return@mapNotNull null
            AdminRouletteHistoryItem.from(history, user.nickname)
        }

        return AdminRouletteHistoryResponse(
            histories = historyItems,
            page = historyPage.number,
            size = historyPage.size,
            totalElements = historyPage.totalElements
        )
    }

    /**
     * 룰렛 취소
     * - 남은 포인트(balance)만 회수 (0으로)
     * - PDP-5: 당일 취소면 DailyBudget remaining 복구
     */
    @Transactional
    fun cancelRoulette(historyId: Long): CancelRouletteResponse {
        val rouletteHistory = rouletteHistoryRepository.findById(historyId)
            .orElseThrow { RouletteNotFoundException() }

        // 이미 취소된 룰렛
        if (!rouletteHistory.isActive()) {
            throw RouletteAlreadyCancelledException()
        }

        // 룰렛 상태 변경
        rouletteHistory.cancel()

        // PointLedger 조회
        val pointLedger = pointLedgerRepository.findByRouletteHistoryId(historyId)
            ?: throw IllegalStateException("PointLedger not found for roulette history")

        val originalAmount = rouletteHistory.amount
        val reclaimedAmount = pointLedger.balance
        val alreadyUsedAmount = originalAmount - reclaimedAmount

        // 남은 포인트만 회수 (balance를 0으로)
        pointLedger.balance = 0

        // PDP-5: 당일 취소면 예산 복구
        val spinDate = rouletteHistory.spinDate
        val cancelDate = LocalDate.now(kstZone)

        var budgetRestored = false
        if (spinDate.isEqual(cancelDate)) {
            // 당일 취소 → 예산 복구
            val budget = dailyBudgetRepository.findByBudgetDate(spinDate)
                ?: throw IllegalStateException("Budget not found for spin date")

            budget.restore(originalAmount)
            budgetRestored = true
        }

        // User 조회 (응답용)
        val user = userRepository.findById(rouletteHistory.userId)
            .orElseThrow { IllegalStateException("User not found") }

        return CancelRouletteResponse(
            historyId = historyId,
            userId = rouletteHistory.userId,
            userName = user.nickname,
            originalAmount = originalAmount,
            reclaimedAmount = reclaimedAmount,
            alreadyUsedAmount = alreadyUsedAmount,
            budgetRestored = budgetRestored,
            message = if (budgetRestored) {
                "Roulette cancelled, ${reclaimedAmount}p reclaimed, budget restored"
            } else {
                "Roulette cancelled, ${reclaimedAmount}p reclaimed (budget not restored - not same day)"
            }
        )
    }
}
