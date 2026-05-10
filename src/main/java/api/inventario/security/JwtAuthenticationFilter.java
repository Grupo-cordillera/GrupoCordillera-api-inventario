package api.inventario.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret.key}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extraer la cabecera "Authorization"
        String authHeader = request.getHeader("Authorization");

        // 2. Si no hay token o no empieza con "Bearer ", dejamos que Spring Security lo bloquee más adelante
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Limpiar el string para quedarnos solo con el token
        String token = authHeader.replace("Bearer ", "");

        try {
            // 4. Validar la firma matemática del token usando tu clave secreta
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 5. Si la firma es correcta, extraemos el usuario (o correo) del token
            String username = claims.getSubject();

            // 6. Le decimos a Spring Security: "Este usuario es legítimo, déjalo pasar"
            if (username != null) {
                // Aquí usamos un ArrayList vacío para los permisos, pero si en tu sistema manejas roles
                // (por ejemplo, diferenciar entre un administrador de TI y un acceso de estudiante),
                // los extraerías del token y los pondrías aquí.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, new ArrayList<>()
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {
            // Si el token expiró, está mal formado o la firma no coincide, la validación falla
            LOGGER.error("Token inválido o expirado: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        // Continuar con la petición
        filterChain.doFilter(request, response);
    }
}