package com.motycka.edu.game.config

import com.motycka.edu.game.account.AccountService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.provisioning.InMemoryUserDetailsManager

@Configuration
@EnableWebSecurity
class SecurityConfiguration(private val userService: AccountService) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.GET, "/login.html").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/accounts").permitAll()
                auth.anyRequest().authenticated()
            }
            .httpBasic(Customizer.withDefaults())
            .logout { logout ->
                logout.permitAll()
            }

        return http.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        // Create the admin user with username "admin" and password "admin"
        val adminUser = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin"))
            .roles("ADMIN", "USER")
            .build()
            
        // Create the in-memory user details manager with the admin user
        val inMemoryUserDetailsManager = InMemoryUserDetailsManager(adminUser)
        
        // Create a custom UserDetailsService that first checks the in-memory users,
        // and if not found, checks the database
        return UserDetailsService { username ->
            try {
                // First try to get the user from in-memory storage
                inMemoryUserDetailsManager.loadUserByUsername(username)
            } catch (e: UsernameNotFoundException) {
                // If not found in memory, try to get from the database
                val user = userService.getByUsername(username)
                    ?: throw UsernameNotFoundException("User not found")

                User.builder()
                    .username(user.username)
                    .password(passwordEncoder().encode(user.password))
                    .roles("USER")
                    .build()
            }
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}


fun main() {
    val password = "heslo"
    val passwordEncoder = BCryptPasswordEncoder()
    val encodedPassword = passwordEncoder.encode(password)
    println(encodedPassword)
}
