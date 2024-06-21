package com.fpt.cursus.util;

import com.fpt.cursus.entity.Account;
import com.fpt.cursus.repository.AccountRepo;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenHandler {
    @Autowired
    private AccountRepo accountRepo;
    @Value("${spring.security.jwt.secret-key}")
    private String secretKey;
    @Value("${spring.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;
    @Value("${spring.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // create token (encode)
    public String generateAccessToken(Account account) {
        return generateToken(account, accessTokenExpiration);
    }
    public String generateRefreshToken(Account account) {
        return generateToken(account, refreshTokenExpiration);
    }
    private String generateToken(Account account, long expiration) {
        Date now = new Date(); // get current time
        Date expirationDate = new Date(now.getTime() + expiration);

        // Create a map to hold custom claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", account.getRole());
        claims.put("name", account.getFullName());
        // Build the token with the additional claims
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(account.getUsername())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }


    public String getInfoByToken(String token) throws ExpiredJwtException, MalformedJwtException {
        String username;
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        username = claims.getSubject();
        // xuống đc đây => token đúng
        return username;
    }
}
