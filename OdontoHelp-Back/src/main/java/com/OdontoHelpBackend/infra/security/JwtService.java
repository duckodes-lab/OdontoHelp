package com.OdontoHelpBackend.infra.security;

import com.OdontoHelpBackend.domain.usuario.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${app.tenant-id:}")
    private String tenantId;

    public String gerarAccessToken(Usuario usuario) {
        var builder = Jwts.builder()
                .subject(usuario.getEmail())
                .claim("perfil", usuario.getPerfil().name())
                .claim("usuarioId", usuario.getId())
                .claim("privacyPolicyVersion", usuario.getPrivacyPolicyVersion())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration));
        if (tenantId != null && !tenantId.isBlank()) {
            builder.claim("tenantId", tenantId.replaceAll("\\D", ""));
        }
        return builder.signWith(getSigningKey()).compact();
    }

    public String extrairEmail(String token) {
        return getClaims(token).getSubject();
    }

    public Long extrairUserId(String token) {
        return getClaims(token).get("usuarioId", Long.class);
    }

    public Instant extrairExpiracao(String token) {
        return getClaims(token).getExpiration().toInstant();
    }

    public boolean accessTokenValido(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String gerarRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public Instant refreshTokenExpiresAt() {
        return Instant.now().plusMillis(refreshExpiration);
    }

    public void validarChaveSecreta() {
        getSigningKey();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(resolveKeyBytes(secret));
    }

    private static byte[] resolveKeyBytes(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("jwt.secret nao configurado (defina JWT_SECRET no .env)");
        }
        String trimmed = raw.trim();
        try {
            byte[] decoded = Decoders.BASE64.decode(trimmed);
            if (decoded.length >= 32) {
                return decoded;
            }
        } catch (Exception ignored) {
            // segue para UTF-8
        }
        byte[] utf8 = trimmed.getBytes(StandardCharsets.UTF_8);
        if (utf8.length < 32) {
            throw new io.jsonwebtoken.security.WeakKeyException(
                    "jwt.secret invalido: use Base64 com 32+ bytes ou texto com 32+ caracteres");
        }
        return utf8;
    }
}
