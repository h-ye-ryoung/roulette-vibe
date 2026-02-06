package com.roulette.common

import org.springframework.http.HttpStatus

open class BusinessException(
    val code: String,
    override val message: String,
    val status: HttpStatus = HttpStatus.BAD_REQUEST
) : RuntimeException(message)

class AlreadyParticipatedException : BusinessException(
    code = "ALREADY_PARTICIPATED",
    message = "You have already participated in today's roulette"
)

class BudgetExhaustedException : BusinessException(
    code = "BUDGET_EXHAUSTED",
    message = "Today's budget has been exhausted"
)

class InsufficientPointsException : BusinessException(
    code = "INSUFFICIENT_POINTS",
    message = "Not enough points"
)

class ProductNotFoundException : BusinessException(
    code = "PRODUCT_NOT_FOUND",
    message = "Product not found",
    status = HttpStatus.NOT_FOUND
)

class ProductOutOfStockException : BusinessException(
    code = "PRODUCT_OUT_OF_STOCK",
    message = "Product is out of stock"
)

class OrderNotFoundException : BusinessException(
    code = "ORDER_NOT_FOUND",
    message = "Order not found",
    status = HttpStatus.NOT_FOUND
)

class OrderAlreadyCancelledException : BusinessException(
    code = "ORDER_ALREADY_CANCELLED",
    message = "Order is already cancelled"
)

class RouletteNotFoundException : BusinessException(
    code = "ROULETTE_NOT_FOUND",
    message = "Roulette history not found",
    status = HttpStatus.NOT_FOUND
)

class RouletteAlreadyCancelledException : BusinessException(
    code = "ROULETTE_ALREADY_CANCELLED",
    message = "Roulette is already cancelled"
)

class RoulettePointsUsedException : BusinessException(
    code = "ROULETTE_POINTS_USED",
    message = "Cannot cancel roulette because points have been used. Cancel orders first."
)

class UnauthorizedException : BusinessException(
    code = "UNAUTHORIZED",
    message = "Not authenticated",
    status = HttpStatus.UNAUTHORIZED
)

class ForbiddenException : BusinessException(
    code = "FORBIDDEN",
    message = "Access denied",
    status = HttpStatus.FORBIDDEN
)
