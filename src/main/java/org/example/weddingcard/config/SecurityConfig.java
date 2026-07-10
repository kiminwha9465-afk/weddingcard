package org.example.weddingcard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/api/cards/*/comments",
                        "/api/cards/*/comments/*",
                        "/api/cards/*/comments/*/toggle-hidden",
                        "/api/cards/*/attendances",
                        "/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/c/**", "/manage/**", "/templates",
                                "/h2-console/**", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/*.png",
                                "/kakao/auth", "/kakao/callback",
                                "/pay/fail").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cards/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cards/*/manage").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cards/*/comments", "/api/cards/*/attendances").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/cards/*/comments/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cards/*/comments/*/toggle-hidden").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }
}
