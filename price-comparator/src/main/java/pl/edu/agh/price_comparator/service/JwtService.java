package pl.edu.agh.price_comparator.service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class JwtService
{
    private static final String CHARACTERS = "abcdef0123456789";
    private static final String SECRET_KEY;

    static
    {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 64; i++)
        {
            key.append(CHARACTERS.charAt(secureRandom.nextInt(CHARACTERS.length())));
        }

        SECRET_KEY = key.toString();
        System.out.println(SECRET_KEY);
    }
// OK
    public String extractUsername(String token)
    {
        return extractClaim(token, Claims::getSubject);
    }
// OK
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
    {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
// OK
    public boolean isTokenValid(String token, UserDetails userDetails)
    {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
// OK
    public boolean isTokenExpired(String token)
    {
        return extractExpiration(token).before(new Date());
    }
// OK
    private Date extractExpiration(String token)
    {
        return extractClaim(token, Claims::getExpiration);
    }
// OK
    public String generateAccessToken(UserDetails userDetails)
    {
        return generateToken(userDetails, 60);
    }
// OK
    public String generateRefreshToken(UserDetails userDetails)
    {
        return generateToken(userDetails, 60*24);
    }
// OK
    private Claims extractAllClaims(String token)
    {
        return Jwts
        .parser()
        .verifyWith(getSignInKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
    }
// OK
    private String generateToken(UserDetails userDetails, long minutes)
    {
        return Jwts
        .builder()
        .subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + minutes * 60 * 1000))
        .signWith(getSignInKey())
        .compact();
    }
// OK
    private SecretKey getSignInKey()
    {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
}