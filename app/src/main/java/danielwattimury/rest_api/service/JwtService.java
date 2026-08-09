package danielwattimury.rest_api.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private String secretKey;

    public JwtService() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            secretKey = Base64.getEncoder().encodeToString(
                    keyGenerator
                            .generateKey()
                            .getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record JWTToken(
            String token,
            Instant expiresAt) {
    }

    public JWTToken generateToken(String username) {
        Instant expiration = Instant.now().plus(30, ChronoUnit.MINUTES);
        return generateToken(username, expiration);
    }

    public JWTToken generateToken(String username, Instant expiration) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();

        String token = Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .and()
                .signWith(getKey())
                .compact();

        return new JWTToken(token, expiration);
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @SuppressWarnings("null")
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUsername(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    @SuppressWarnings("null")
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
