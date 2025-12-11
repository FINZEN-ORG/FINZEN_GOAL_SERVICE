package eci.ieti.FinzenGoalService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;
    private final String secret = "01234567890123456789012345678901"; // 32 chars

    @BeforeEach
    void setUp() throws Exception {
        provider = new JwtTokenProvider();
        setField(provider, "jwtSecret", secret);
        setField(provider, "jwtExpirationInMs", 3600000);
        provider.init();
    }

    @Test
    void validateToken_trueForValidToken() throws Exception {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        String token = Jwts.builder()
                .setSubject("user1")
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThat(provider.validateToken(token)).isTrue();
        Claims claims = provider.getClaims(token);
        assertThat(claims.getSubject()).isEqualTo("user1");
    }

    @Test
    void validateToken_falseForBadToken() {
        assertThat(provider.validateToken("bad-token")).isFalse();
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
