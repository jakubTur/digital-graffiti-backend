package edu.w04ist.si041g.backend.auth;

import edu.w04ist.si041g.backend.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;


    public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        try {
            var claims = jwtService.parseToken(token);
            if (!jwtService.isAccessToken(claims)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "access token required");
                return;
            }
            Long userId = Long.parseLong(claims.getSubject());
            var user = userService.getUserById(userId);
            if (!user.isActive()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "User account is inactive");
                return;
            }
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name().toUpperCase()));
            var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
        }
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path  = request.getServletPath();
        return path.equals("/auth/login")
                || path.equals("/api/users/register")
                || (path.equals("/api/credentials")
                && request.getMethod().equals("POST"));
    }
}
