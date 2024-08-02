package com.fpt.cursus.config;

import com.fpt.cursus.entity.Account;
import com.fpt.cursus.exception.exceptions.AuthException;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.repository.BlackListIpRepo;
import com.fpt.cursus.service.ApiLogService;
import com.fpt.cursus.service.IpLogService;
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
import java.util.HashSet;
import java.util.Set;

@Component
public class Filter extends OncePerRequestFilter {
    private final HandlerExceptionResolver resolver;
    private final TokenHandler tokenHandler;
    private final AccountRepo accountRepo;
    private final IpLogService ipLogService;
    private final ApiLogService apiLogService;
    private final BlackListIpRepo blackListIpRepo;
    private final Set<String> whitelistUris;

    @Autowired
    public Filter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
                  TokenHandler tokenHandler,
                  AccountRepo accountRepo,
                  IpLogService ipLogService,
                  BlackListIpRepo blackListIpRepo,
                  ApiLogService apiLogService) {
        this.resolver = resolver;
        this.tokenHandler = tokenHandler;
        this.accountRepo = accountRepo;
        this.ipLogService = ipLogService;
        this.blackListIpRepo = blackListIpRepo;
        this.apiLogService = apiLogService;
        whitelistUris = new HashSet<>();
        whitelistUris.add("/login");
        whitelistUris.add("/register");
        whitelistUris.add("/swagger-ui");
        whitelistUris.add("/v3");
        whitelistUris.add("/auth");
        whitelistUris.add("/token");
        whitelistUris.add("/order/update-status");
        whitelistUris.add("/course/view-all-general");
        whitelistUris.add("/course/view-general-by-name");
        whitelistUris.add("/course/view-general-by-category");
        whitelistUris.add("/category");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String ipAddress = request.getRemoteAddr();
        // Check ip ban
        if (blackListIpRepo.findByIpAddress(ipAddress).isPresent()) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Your IP is banned due to excessive requests.");
            return;
        }

        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null) {
            apiLogService.saveApiLog(uri, request.getQueryString());
        }
        // Save log ip
        ipLogService.logAccess(ipAddress, uri);

        String token = getToken(request);

        boolean isWhitelisted = whitelistUris.stream().anyMatch(uri::contains);
        if (isWhitelisted) {
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
