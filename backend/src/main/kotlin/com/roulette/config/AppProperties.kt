package com.roulette.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val adminNicknames: String = "admin",
    val dailyBudgetDefault: Int = 100000
) {
    fun getAdminNicknameList(): List<String> =
        adminNicknames.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun isAdmin(nickname: String): Boolean =
        getAdminNicknameList().contains(nickname)
}
