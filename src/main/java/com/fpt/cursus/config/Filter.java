package com.fpt.cursus.config;

import com.fpt.cursus.entity.Account;
import com.fpt.cursus.exception.exceptions.AuthException;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.repository.BackListIPRepo;
import com.fpt.cursus.service.ApiLogService;
import com.fpt.cursus.service.impl.ApiLogServiceImpl;
import com.fpt.cursus.util.TokenHandler;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class Filter extends OncePerRequestFilter {
    private final HandlerExceptionResolver resolver;
    private final TokenHandler tokenHandler;
    private final AccountRepo accountRepo;
    private final ApiLogService apiLogService;
    private final BackListIPRepo backListIPRepo;

    @Autowired
    public Filter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
                  TokenHandler tokenHandler,
                  AccountRepo accountRepo, ApiLogService apiLogService, BackListIPRepo backListIPRepo) {
        this.resolver = resolver;
        this.tokenHandler = tokenHandler;
        this.accountRepo = accountRepo;
        this.apiLogService = apiLogService;
        this.backListIPRepo = backListIPRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ipAddress = request.getRemoteAddr();
        // Kiểm tra nếu IP bị cấm
        if (backListIPRepo.findByIpAddress(ipAddress).isPresent()) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Your IP is banned due to excessive requests.");
            return;
        }

        // Ghi log truy cập
        apiLogService.logAccess(ipAddress, request.getRequestURI());


        String token = getToken(request);
        String uri = request.getRequestURI();
        if (uri.contains("login") || uri.contains("register") || uri.contains("swagger-ui") || uri.contains("v3")
                || uri.contains("auth") || uri.contains("token") || uri.contains("order/update-status") ) {
            filterChain.doFilter(request, response);
        } else {
            if (token == null) {
                resolver.resolveException(request, response, null, new AuthException("Empty Token", HttpStatus.UNAUTHORIZED.value()));
                return;
            }
            String username;
            try {
                username = tokenHandler.getInfoByToken(token);
            } catch (SignatureException | ExpiredJwtException e) {
                resolver.resolveException(request, response, null, new AuthException(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
                return;
            }
            Account account = accountRepo.findAccountByUsername(username);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        }
    }

    public String getFullURL(HttpServletRequest request) {
        StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
        String queryString = request.getQueryString();

        if (queryString != null) {
            requestURL.append("?").append(queryString);
        }

        return requestURL.toString();
    }

    public String getToken(HttpServletRequest request) {
        try {
            return request.getHeader("Authorization").split(" ")[1];
        } catch (Exception e) {
            return null;
        }
    }
}
