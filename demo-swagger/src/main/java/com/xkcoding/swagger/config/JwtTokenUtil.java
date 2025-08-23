package com.xkcoding.swagger.config;

import com.xkcoding.swagger.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * JWT Token 工具类
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Slf4j
@Component
public class JwtTokenUtil {

    /**
     * JWT 签名密钥
     */
    @Value("${jwt.secret:demo-swagger-security-jwt-secret-key-2024}")
    private String jwtSecret;

    /**
     * JWT 过期时间（小时）
     */
    @Value("${jwt.expiration:24}")
    private int jwtExpirationInHours;

    /**
     * JWT 刷新Token过期时间（天）
     */
    @Value("${jwt.refresh-expiration:7}")
    private int jwtRefreshExpirationInDays;

    /**
     * JWT Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * JWT Token Header 名称
     */
    public static final String HEADER_STRING = "Authorization";

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 根据用户信息生成Token
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("name", user.getName());
        claims.put("email", user.getEmail());
        
        // 添加权限信息
        if (user.getAuthorities() != null) {
            List<String> authorities = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            claims.put("authorities", authorities);
        }
        
        // 添加角色信息
        if (user.getRoles() != null) {
            List<String> roles = user.getRoles().stream()
                    .map(role -> role.getRoleCode())
                    .collect(Collectors.toList());
            claims.put("roles", roles);
        }

        return createToken(claims, user.getUsername());
    }

    /**
     * 根据认证信息生成Token
     */
    public String generateToken(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();
        return generateToken(userPrincipal);
    }

    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createRefreshToken(claims, username);
    }

    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInHours * 60 * 60 * 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 创建刷新Token
     */
    private String createRefreshToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshExpirationInDays * 24 * 60 * 60 * 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.warn("无法从Token中获取用户名: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public Integer getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object userId = claims.get("userId");
            return userId != null ? (Integer) userId : null;
        } catch (Exception e) {
            log.warn("无法从Token中获取用户ID: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getAuthoritiesFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return (List<String>) claims.get("authorities");
        } catch (Exception e) {
            log.warn("无法从Token中获取权限信息: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return (List<String>) claims.get("roles");
        } catch (Exception e) {
            log.warn("无法从Token中获取角色信息: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.warn("无法从Token中获取过期时间: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取Claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token, String username) {
        try {
            if (!StringUtils.hasText(token)) {
                return false;
            }

            String tokenUsername = getUsernameFromToken(token);
            return tokenUsername != null && 
                   tokenUsername.equals(username) && 
                   !isTokenExpired(token);
        } catch (SecurityException e) {
            log.error("JWT签名无效: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JWT格式错误: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT不支持: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT参数错误: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT验证失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 验证Token（不检查用户名）
     */
    public boolean validateToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return false;
            }

            getClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            claims.setIssuedAt(new Date());
            claims.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInHours * 60 * 60 * 1000L));
            
            return Jwts.builder()
                    .setClaims(claims)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            log.error("Token刷新失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从请求头中解析Token
     */
    public String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 获取Token剩余有效时间（毫秒）
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            if (expiration != null) {
                return expiration.getTime() - System.currentTimeMillis();
            }
        } catch (Exception e) {
            log.warn("获取Token剩余时间失败: {}", e.getMessage());
        }
        return 0L;
    }

    /**
     * 检查Token是否即将过期（1小时内）
     */
    public boolean isTokenExpiringSoon(String token) {
        long remainingTime = getTokenRemainingTime(token);
        return remainingTime > 0 && remainingTime < 60 * 60 * 1000L; // 1小时
    }
}