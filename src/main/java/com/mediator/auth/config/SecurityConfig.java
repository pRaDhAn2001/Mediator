package com.mediator.auth.config;

import com.mediator.auth.security.JwtAuthenticationEntryPoint;
import com.mediator.auth.security.JwtAuthenticationFilter;
import com.mediator.auth.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final CustomUserDetailsService userDetailsService;
        private final PasswordEncoder passwordEncoder;
        private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

        public SecurityConfig(
                        JwtAuthenticationFilter jwtAuthFilter,
                        CustomUserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder,
                        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
                this.jwtAuthFilter = jwtAuthFilter;
                this.userDetailsService = userDetailsService;
                this.passwordEncoder = passwordEncoder;
                this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http)
                        throws Exception {

                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(Customizer.withDefaults())

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))

                                .authorizeHttpRequests(auth -> auth

                                                // Public endpoints
                                                .requestMatchers(
                                                                "/api/v1/auth/login",
                                                                "/api/v1/auth/register/**",
                                                                "/api/v1/subscriptions/plans",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/v3/api-docs/**",
                                                                "/v3/api-docs.yaml")
                                                .permitAll()

                                                // Student endpoints
                                                .requestMatchers("/api/v1/students/**")
                                                .hasRole("STUDENT")

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/v1/matches")
                                                .hasRole("STUDENT")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/v1/matches/student")
                                                .hasRole("STUDENT")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/v1/matches/*/cancel")
                                                .hasRole("STUDENT")

                                                .requestMatchers("/api/v1/search/**")
                                                .hasRole("STUDENT")

                                                // Tutor endpoints
                                                .requestMatchers("/api/v1/tutors/**")
                                                .hasRole("TUTOR")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/v1/matches/tutor")
                                                .hasRole("TUTOR")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/v1/matches/*/connect",
                                                                "/api/v1/matches/*/partial-finalize",
                                                                "/api/v1/matches/*/finalize")
                                                .hasRole("TUTOR")

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/v1/subscriptions")
                                                .hasRole("TUTOR")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/v1/subscriptions/current",
                                                                "/api/v1/subscriptions/history")
                                                .hasRole("TUTOR")

                                                // Match details: student or tutor, with ownership
                                                // checked inside MatchRequestService.
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/v1/matches/*")
                                                .hasAnyRole("STUDENT", "TUTOR")

                                                // Logout requires authentication. With stateless JWT,
                                                // this endpoint alone does not revoke an existing token.
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/v1/auth/logout")
                                                .authenticated()

                                                // All other endpoints require authentication.
                                                .anyRequest().authenticated())

                                .authenticationProvider(authenticationProvider())

                                .addFilterBefore(
                                                jwtAuthFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder);

                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }
}
