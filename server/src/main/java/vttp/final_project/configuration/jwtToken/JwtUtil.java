package vttp.final_project.configuration.jwtToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // Limits time duration from theft/session hijacking
    // Forces reauthentication for better security
    @Value("${jwt.expiration}")
    private long expiration;

    // Generate secret key from our secret string
    private Key getSigningKey() {
        // Converts the secret string into a byte array using UTF-8 encoding
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // Creates a HMAC-SHA key
        // Key is used to sign new tokens or verify existing ones
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate token for user
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims) // Any custom data
                .setSubject(subject) // Primary Identifier: email
                .setIssuedAt(new Date(System.currentTimeMillis())) // Sets time of token creation
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Sets the expiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Signs token with secret key and HMAC-SHA256 alogrithm
                .compact(); // Serializes everything into the final JWT string

        /*  Resulting token is a string with three parts separated by dots (header.payload.signature)
        e.g. eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0.rEf6GdgGMrDpxyS7V9ZP9wvnTzwk5jrVSFZRGBzW7ZE */
    }

    // Validate token
    public Boolean validateToken(String token, String email) {
        final String username = extractEmail(token);
        // Confirms email in token matches expected user email and token is not expired
        return (username.equals(email) && !isTokenExpired(token));
    }

    /* Data Extraction helper methods */
    // Extract email from token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    // Gets all data
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()// Creates a parser
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)// Parses the token and verify signature
                .getBody(); // Returns payload(body) of token that contains the claims
    }

    // Check if token expiration is in the past
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /* The Big Picture
    This code implements the core JWT lifecycle:

    Creation: When a user successfully logs in, you generate a token containing their email
    Transmission: The token is sent to the client (typically in an HTTP response)
    Storage: The client stores the token (often in browser storage)
    Usage: For subsequent requests, the client includes the token (typically in the Authorization header)
    Validation: Your server validates the token to authenticate the request
    Extraction: You extract the user's identity from the token to authorize appropriate access*/
}
