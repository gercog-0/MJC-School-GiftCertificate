package com.epam.esm.service.impl.security.provider;

import com.epam.esm.dao.api.entity.Role;
import com.epam.esm.service.api.dto.RoleDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final String AUTHORIZATION_BEARER = "Bearer_";
    private static final int AUTHORIZATION_BEARER_LENGTH = 7;

    @Value("${jwt.token.secret}")
    private String secret;

    @Value("${jwt.token.expired}")
    private Long validityInMilliseconds;

    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtTokenProvider(@Qualifier("jwtUserDetailsService") UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public String create(String login, List<RoleDto> roles) {
        Claims claims = Jwts.claims().setSubject(login);
        claims.put("roles", getRoleNames(roles));
        Date dateNow = new Date();
        Date dateExpired = new Date(dateNow.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(dateNow)
                .setExpiration(dateExpired)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public Optional<String> resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken == null || !bearerToken.startsWith(AUTHORIZATION_BEARER)){
            return Optional.empty();
        }
        return Optional.of(bearerToken.substring(AUTHORIZATION_BEARER_LENGTH));
    }

    public boolean isTokenValid(String token) {
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
        return !claimsJws.getBody().getExpiration().before(new Date());
    }

    private String getUsername(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }

    private List<String> getRoleNames(List<RoleDto> roles) {
        return roles.stream()
                .map(RoleDto::getName)
                .collect(Collectors.toList());
    }
}
