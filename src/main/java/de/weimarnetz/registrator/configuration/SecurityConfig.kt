package de.weimarnetz.registrator.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager

const val ADMIN_ROLE = "ADMIN"

@Configuration
class SecurityConfig : WebSecurityConfigurerAdapter() {
    @Value("\${admin.user}")
    private val username: String? = null

    @Value("\${admin.password}")
    private val password: String? = null

    override fun configure(http: HttpSecurity) {
        http
            .headers().frameOptions().sameOrigin()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .httpBasic()
            .and()
            .csrf().disable()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/dumpDatabase").hasRole(ADMIN_ROLE)
            .antMatchers(HttpMethod.GET).permitAll()
            .antMatchers(HttpMethod.POST, "/importDatabase").hasRole(ADMIN_ROLE)
            .antMatchers(HttpMethod.POST).permitAll()
            .antMatchers(HttpMethod.PUT).permitAll()
            .antMatchers(HttpMethod.OPTIONS).permitAll()
            .regexMatchers("/(css|js|fonts)/.*").permitAll()
            .antMatchers(HttpMethod.DELETE).hasRole(ADMIN_ROLE)
            .anyRequest().authenticated()
    }

    @Bean
    public override fun userDetailsService(): UserDetailsService? {
        // Set the inMemoryAuthentication object with the given credentials:
        val manager = InMemoryUserDetailsManager()
        val encodedPassword = passwordEncoder().encode(password)
        manager.createUser(User.withUsername(username).password(encodedPassword).roles(ADMIN_ROLE).build())
        return manager
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}