package com.bufalari.people.config;

import com.bufalari.people.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor // Agora só injeta dependências EXTERNAS à classe via construtor
public class SecurityConfig {

    // Dependência externa injetada via construtor
    private final JwtAuthFilter jwtAuthFilter;

    // Define public endpoints
    private static final String[] PUBLIC_MATCHERS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/health",
    };

    /**
     * Configura a cadeia de filtros de segurança principal.
     * @param http O objeto HttpSecurity para configurar.
     * @param authenticationProvider O AuthenticationProvider definido como bean nesta classe.
     * @param corsConfigurationSource A fonte de configuração CORS definida como bean nesta classe.
     * @return O SecurityFilterChain construído.
     * @throws Exception Se ocorrer um erro na configuração.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                 AuthenticationProvider authenticationProvider, // Injetado como parâmetro
                                                 CorsConfigurationSource corsConfigurationSource) throws Exception { // Injetado como parâmetro
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource)) // Usa o parâmetro injetado
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_MATCHERS).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/persons/**").authenticated()
                .requestMatchers("/api/groups/**").authenticated()
                .requestMatchers("/api/subgroups/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider) // Usa o parâmetro injetado
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Usa o campo injetado no construtor

        return http.build();
    }

    /**
     * Define o AuthenticationProvider.
     * @param userDetailsService O serviço UserDetailsService (será injetado pelo Spring).
     * @return Um bean AuthenticationProvider configurado.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        // authProvider.setPasswordEncoder(passwordEncoder()); // Não necessário para auth JWT pura
        return authProvider;
    }

    /**
     * Define a configuração CORS.
     * @return Um bean CorsConfigurationSource.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Permite qualquer origem - RESTRINJA EM PRODUÇÃO!
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}