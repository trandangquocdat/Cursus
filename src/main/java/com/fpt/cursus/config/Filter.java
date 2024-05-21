package com.fpt.cursus.config;

import com.fpt.cursus.entity.Account;
import com.fpt.cursus.exception.exceptions.AuthenticationException;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.util.TokenHandler;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class Filter extends OncePerRequestFilter {
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;
    @Autowired
    TokenHandler tokenHandler;
    @Autowired
    AccountRepo accountRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getToken(request);
        String uri = request.getRequestURI();
        if (uri.contains("login") || uri.contains("register") || uri.contains("swagger-ui") || uri.contains("v3")) {
            filterChain.doFilter(request, response);
        }else{
            if(token == null){
                resolver.resolveException(request, response, null, new AuthenticationException("Empty Token"));
                return;
            }
            String username;
            try{
                username = tokenHandler.getInfoByToken(token);
            }catch (SignatureException e){
                resolver.resolveException(request, response, null, new AuthenticationException(e.getMessage()));
                return;
            }catch (ExpiredJwtException e){
                resolver.resolveException(request, response, null, new AuthenticationException(e.getMessage()));
                return;
            }
            Account account = accountRepo.findAccountByUsername(username);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        }
    }

    public String getToken(HttpServletRequest request){
        try{
            String token = request.getHeader("Authorization").split(" ")[1];
            return token;
        }catch(Exception e){
            return null;
        }
    }
}
