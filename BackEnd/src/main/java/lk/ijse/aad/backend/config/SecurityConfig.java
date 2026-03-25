package lk.ijse.aad.backend.config;

import lk.ijse.aad.backend.utill.JWTAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth

                        // ── Swagger ─────────────────────────────────────────────
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ── Auth ─────────────────────────────────────────────────
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // ── Vehicles: public GET ──────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/v1/vehicles/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/vehicles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,  "/api/v1/vehicles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/vehicles/**").hasRole("ADMIN")

                        // ── PayHere notify: PUBLIC ────────────────────────────────
                        // ⚠️ MUST be BEFORE the broad /api/v1/payments/** rule
                        // PayHere servers POST here — they have no JWT token
                        // Security is done by MD5 hash verification in service
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/payhere/notify").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/v1/payments/payhere/status").permitAll()

                        // ── All other payments: authenticated ─────────────────────
                        .requestMatchers("/api/v1/payments/**").authenticated()

                        // ── Bookings: authenticated ───────────────────────────────
                        .requestMatchers("/api/v1/bookings/**").authenticated()

                        // ── Admin only ────────────────────────────────────────────
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/insurances/**").hasRole("ADMIN")

                        // ── User profile ──────────────────────────────────────────
                        .requestMatchers("/api/v1/user/profile").authenticated()
                        .requestMatchers("/payment-success.html", "/payment-cancel.html").permitAll()

                        // ── Everything else ───────────────────────────────────────
                        .anyRequest().authenticated()
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}