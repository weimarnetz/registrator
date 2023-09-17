package de.weimarnetz.registrator.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain


const val ADMIN_ROLE = "ADMIN"

@Configuration
class SecurityConfig {
    @Value("\${admin.user}")
    private val username: String? = null

    @Value("\${admin.password}")
    private val password: String? = null

    @Bean
    fun configure(http: HttpSecurity): SecurityFilterChain {
        return http
            .headers { headersConfigurer ->
                headersConfigurer.frameOptions { it.sameOrigin() }
            }.sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.csrf { it.disable() }
            .httpBasic {}
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.GET, "/dumpDatabase").hasRole(ADMIN_ROLE)
                it.requestMatchers(HttpMethod.GET).permitAll()
                it.requestMatchers(HttpMethod.POST, "/importDatabase").hasRole(ADMIN_ROLE)
                it.requestMatchers(HttpMethod.POST).permitAll()
                it.requestMatchers(HttpMethod.PUT).permitAll()
                it.requestMatchers(HttpMethod.OPTIONS).permitAll()
                it.requestMatchers("/(css|js|fonts)/.*").permitAll()
                it.requestMatchers(HttpMethod.DELETE).hasRole(ADMIN_ROLE)
                it.anyRequest().authenticated()
            }.build()

    }


    @Bean
    fun userDetailsService(): InMemoryUserDetailsManager {
        val encodedPassword = passwordEncoder().encode(password)
        val user = User
            .withUsername(username)
            .password(encodedPassword)
            .roles(ADMIN_ROLE)
            .build()
        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}