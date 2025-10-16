package com.kyskfilms.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val keycloakProperties: KeycloakProperties
) {

    @Bean
    @Order(1)
    fun publicFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/actuator/health",
                "/actuator/info"
            )
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }

        return http.build()
    }

    @Bean
    @Order(2)
    fun apiFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/api/**")
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
            .authorizeHttpRequests { auth ->
                auth
                    // Public endpoints
                    .requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/genres/**").permitAll()

                    // Admin endpoints
                    .requestMatchers(HttpMethod.POST, "/api/movies/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/movies/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/movies/**").hasRole("ADMIN")
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    // User endpoints
                    .requestMatchers("/api/users/**").authenticated()

                    // Default
                    .anyRequest().authenticated()
            }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { _, response, _ ->
                        response.status = 401
                        response.contentType = "application/json"
                        response.writer.write("""{"error": "Unauthorized", "message": "Valid JWT token required"}""")
                    }
                    .accessDeniedHandler { _, response, _ ->
                        response.status = 403
                        response.contentType = "application/json"
                        response.writer.write("""{"error": "Forbidden", "message": "Insufficient privileges"}""")
                    }
            }

        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(KeycloakRoleConverter(keycloakProperties.clientId))
        return converter
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val jwkSetUri = "${keycloakProperties.authServerUrl}/realms/${keycloakProperties.realm}/protocol/openid-connect/certs"
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("http://localhost:*", "http://127.0.0.1:*")
            allowedMethods = keycloakProperties.corsAllowedMethods.split(",").map { it.trim() }
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Authorization", "Link", "X-Total-Count")
            allowCredentials = true
            maxAge = keycloakProperties.corsMaxAge.toLong()
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}