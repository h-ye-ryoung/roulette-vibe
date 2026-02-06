package com.roulette.domain.order

import com.roulette.common.InsufficientPointsException
import com.roulette.common.ProductNotFoundException
import com.roulette.common.ProductOutOfStockException
import com.roulette.domain.order.dto.CreateOrderResponse
import com.roulette.domain.order.dto.OrderItemResponse
import com.roulette.domain.order.dto.OrderListResponse
import com.roulette.domain.point.PointLedgerRepository
import com.roulette.domain.product.ProductRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderPointUsageRepository: OrderPointUsageRepository,
    private val productRepository: ProductRepository,
    private val pointLedgerRepository: PointLedgerRepository
) {

    /**
     * 상품 주문 생성
     *
     * 동작 순서:
     * 1. Product 조회 및 재고 원자적 차감
     * 2. 유효 포인트 FIFO 조회 (SELECT FOR UPDATE)
     * 3. 포인트 부족 체크
     * 4. FIFO 순으로 balance 차감 (N개 PointLedger UPDATE)
     * 5. Order INSERT
     * 6. OrderPointUsage INSERT (N건)
     */
    @Transactional
    fun createOrder(userId: Long, productId: Long): CreateOrderResponse {
        val now = LocalDateTime.now()

        // 1. Product 조회
        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException() }

        val totalPrice = product.price

        // 2. 재고 원자적 차감 (WHERE stock > 0)
        val updatedRows = productRepository.decrementStock(productId)
        if (updatedRows == 0) {
            throw ProductOutOfStockException()
        }

        // 3. 유효 포인트 FIFO 조회 (SELECT ... FOR UPDATE)
        val availablePoints = pointLedgerRepository.findAvailableByUserIdForUpdate(userId, now)

        // 4. 포인트 부족 체크
        val totalAvailable = availablePoints.sumOf { it.balance }
        if (totalAvailable < totalPrice) {
            // 롤백: 재고 복원
            productRepository.incrementStock(productId)
            throw InsufficientPointsException()
        }

        // 5. FIFO 순으로 balance 차감
        var remainingAmount = totalPrice
        val usages = mutableListOf<OrderPointUsage>()

        for (pointLedger in availablePoints) {
            if (remainingAmount <= 0) break

            val deductAmount = pointLedger.deduct(remainingAmount)
            remainingAmount -= deductAmount

            // OrderPointUsage 레코드 생성 (나중에 일괄 저장)
            usages.add(
                OrderPointUsage(
                    orderId = 0, // 임시, Order 저장 후 업데이트
                    pointLedgerId = pointLedger.id ?: throw IllegalStateException("PointLedger ID is null"),
                    usedAmount = deductAmount
                )
            )
        }

        // 6. Order INSERT
        val order = Order(
            userId = userId,
            productId = productId,
            totalPrice = totalPrice
        )
        val savedOrder = orderRepository.save(order)

        // 7. OrderPointUsage INSERT (N건)
        usages.forEach { usage ->
            orderPointUsageRepository.save(
                OrderPointUsage(
                    orderId = savedOrder.id,
                    pointLedgerId = usage.pointLedgerId,
                    usedAmount = usage.usedAmount
                )
            )
        }

        // 8. 잔여 포인트 계산
        val remainingBalance = pointLedgerRepository.sumAvailableBalance(userId, now)

        return CreateOrderResponse(
            orderId = savedOrder.id,
            productName = product.name,
            totalPrice = totalPrice,
            remainingBalance = remainingBalance
        )
    }

    /**
     * 내 주문 내역 조회 (페이지네이션)
     */
    fun getOrders(userId: Long, page: Int, size: Int): OrderListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val orderPage = orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable)

        // Product 정보 조회를 위해 productId 수집
        val productIds = orderPage.content.map { it.productId }.toSet()
        val products = productRepository.findAllById(productIds).associateBy { it.id }

        val orderItems = orderPage.content.map { order ->
            val product = products[order.productId]
                ?: throw ProductNotFoundException() // 정합성 에러 방지
            OrderItemResponse.from(order, product)
        }

        return OrderListResponse(
            orders = orderItems,
            page = orderPage.number,
            size = orderPage.size,
            totalElements = orderPage.totalElements
        )
    }
}
