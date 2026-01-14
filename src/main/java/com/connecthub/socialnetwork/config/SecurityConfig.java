package com.connecthub.socialnetwork.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Configuration de sécurité Spring Security
 * Gère l'authentification, l'autorisation et la protection CSRF
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Configuration CSRF : désactivé pour les API REST, activé pour les formulaires
                                // HTML
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                // Avec le nouveau PathPatternParser, les ** ne peuvent pas être au
                                                // milieu.
                                                // On cible donc explicitement les endpoints REST utilisés par AJAX.
                                                .ignoringRequestMatchers(
                                                                "/api/**",
                                                                "/posts/**",
                                                                "/friends/**"))
                                // Configuration des autorisations
                                .authorizeHttpRequests(auth -> auth
                                                // Routes publiques
                                                .requestMatchers("/", "/index", "/login", "/register", "/css/**",
                                                                "/js/**", "/images/**", "/static/**", "/api/test")
                                                .permitAll()
                                                // Toutes les autres routes nécessitent une authentification
                                                .anyRequest().authenticated())
                                // Configuration du formulaire de connexion
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/home", true)
                                                .failureUrl("/login?error=true")
                                                .permitAll()
                                                .usernameParameter("username") // Le champ username du formulaire
                                                                               // (email)
                                                .passwordParameter("password") // Le champ password du formulaire
                                )
                                // Gestion des erreurs d'accès
                                .exceptionHandling(exceptions -> exceptions
                                                .accessDeniedPage("/login?error=access_denied"))
                                // Configuration de la déconnexion
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll())
                                // Gestion des sessions
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }
}