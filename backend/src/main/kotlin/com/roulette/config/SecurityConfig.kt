package com.roulette.config

import com.roulette.auth.SessionAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val sessionAuthenticationFilter: SessionAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { cors ->
                cors.configurationSource {
                    org.springframework.web.cors.CorsConfiguration().apply {
                        allowedOriginPatterns = listOf(
                            "http://localhost:*",     // 로컬 개발 (모든 포트)
                            "https://*.vercel.app"    // Vercel 배포 (프론트엔드/어드민)
                        )
                        allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        allowedHeaders = listOf("*")
                        allowCredentials = true
                        maxAge = 3600
                    }
                }
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/admin/**").permitAll()  // 어드민은 인증 불필요
                    .requestMatchers("/api/user/**").authenticated()  // 사용자는 세션 인증 필요
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**"
                    ).permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                ex.accessDeniedHandler { _, response, _ ->
                    response.status = HttpStatus.FORBIDDEN.value()
                    response.contentType = "application/json"
                    response.writer.write("""{"success":false,"error":{"code":"FORBIDDEN","message":"Access denied"}}""")
                }
            }

        return http.build()
    }
}
