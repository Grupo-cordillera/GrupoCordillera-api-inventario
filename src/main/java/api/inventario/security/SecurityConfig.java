package api.inventario.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Apagamos CSRF porque las APIs REST con JWT no lo necesitan
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Le decimos a Spring que no guarde sesiones en memoria (STATELESS), cada petición debe traer su JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Configuramos qué rutas proteger
                .authorizeHttpRequests(auth -> auth
                        // Si quisieras dejar una ruta pública, lo harías así:
                        // .requestMatchers("/api/inventario/publico/**").permitAll()
                        // Endpoints públicos para Swagger / OpenAPI 3
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )

                // 4. Colocamos nuestro filtro ANTES del filtro tradicional de Spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}