package com.fpt.cursus.util;

import com.fpt.cursus.entity.Account;
import com.fpt.cursus.repository.AccountRepo;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenHandler {
    @Autowired
    private AccountRepo accountRepo;
    private final String SECRET_KEY = "cursus";
    //    1s => 1000ms
    //    private final UUID EXPIRATION = 1 * 60 * 1000;
    private final long ACCESS_TOKEN_EXPIRATION = 1 * 24 * 60 * 60 * 1000;
    private final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    // create token (encode)
    public String generateToken(Account account) {
        Date now = new Date(); // get current time
        Date expirationDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

        // Create a map to hold custom claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", account.getRole());
        claims.put("name", account.getFullName());

        // Build the token with the additional claims
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(account.getUsername())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();

        return token;
    }
    public String generateRefreshToken(Account account) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);

        // Build the refresh token
        String refreshToken = Jwts.builder()
                .setSubject(account.getUsername())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();

        return refreshToken;
    }

    public String refreshAccessToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(refreshToken).getBody();
            String username = claims.getSubject();
            // Generate new access token
            // Assuming you have a method to get the Account by username
            Account account = accountRepo.findAccountByUsername(username);
            return generateToken(account);
        } catch (ExpiredJwtException | MalformedJwtException e) {
            throw new RuntimeException("Invalid refresh token");
        }
    }
    // validate token
    // get info from token (decode)
    public String getInfoByToken(String token) throws ExpiredJwtException, MalformedJwtException {
        String username;
        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        username = claims.getSubject();
        // xuống đc đây => token đúng
        return username;
    }
}
