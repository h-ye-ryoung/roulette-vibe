package com.roulette.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val dailyBudgetDefault: Int = 100000
)
