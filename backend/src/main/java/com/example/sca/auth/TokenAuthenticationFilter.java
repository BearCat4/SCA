package com.example.sca.auth;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenService tokenService;

    public TokenAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Optional<UserPrincipal> principal = tokenService.parseUserToken(header.substring(7));
            if (principal.isPresent()) {
                request.setAttribute("currentUser", principal.get());
            }
        } else if (request.getParameter("access_token") != null) {
            Optional<UserPrincipal> principal = tokenService.parseUserToken(request.getParameter("access_token"));
            if (principal.isPresent()) {
                request.setAttribute("currentUser", principal.get());
            }
        }
        filterChain.doFilter(request, response);
    }
}
