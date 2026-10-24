package com.studyhub.security;

import com.studyhub.repositories.UtenteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Al login Spring Security chiede a questo servizio l'hash della password e
    // il ruolo dell'utente; i dati vengono dal database tramite JPA.
    @Bean
    public UserDetailsService userDetailsService(UtenteRepository utenteRepository) {
        return username -> utenteRepository.findByUsername(username)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getPassword())
                        .roles(u.getRuolo().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));
    }

    // Usato da AuthController.login per verificare username e password.
    // Se lo username non esiste, DaoAuthenticationProvider calcola comunque un
    // hash BCrypt: i tempi di risposta non rivelano quali username esistono.
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    // L'utente autenticato viene salvato nella sessione HTTP lato server; il
    // browser riceve solo il cookie JSESSIONID (HttpOnly).
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            SecurityContextRepository securityContextRepository) throws Exception {
        http
                .cors(Customizer.withDefaults())
                // CSRF con "double submit cookie": il token arriva nel cookie
                // XSRF-TOKEN (leggibile da JavaScript) e il frontend lo rimanda
                // nell'header X-XSRF-TOKEN per ogni richiesta che modifica dati.
                // Un altro sito non può leggere il cookie, quindi non può
                // costruire l'header.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                // Niente salvataggio della richiesta rifiutata per il redirect
                // dopo il login (utile solo alle pagine HTML): così una sessione
                // nasce solo al login, non a ogni richiesta anonima.
                .requestCache(cache -> cache.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/moderatore/**").hasRole("MODERATORE")
                        .requestMatchers(HttpMethod.POST, "/api/segnalazioni").hasRole("UTENTE")
                        .anyRequest().authenticated())
                // Richiesta senza sessione: 401 senza corpo invece della pagina
                // di login HTML pensata per le app server-side.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()));
        return http.build();
    }
}
