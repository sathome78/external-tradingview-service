package me.exrates.externalservice.web.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.properties.SecurityProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public abstract class AbstractJwtAuthenticationProcessFilter extends GenericFilterBean {

    private static final String AUTH_PREFIX = "Bearer";

    private final SecurityProperty securityProperty;

    protected AbstractJwtAuthenticationProcessFilter(SecurityProperty securityProperty) {
        this.securityProperty = securityProperty;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        String header = request.getHeader("Authorization");
        String queryToken = request.getParameter("authorization");
        String token = null;
        if (StringUtils.isNotEmpty(header)) {
            if (!header.startsWith(AUTH_PREFIX)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            token = header.replace(AUTH_PREFIX, StringUtils.EMPTY).trim();
        } else if (StringUtils.isNotEmpty(queryToken)) {
            token = queryToken.trim();
        }
        if (StringUtils.isNotEmpty(token)) {
            try {
                JWTVerifier verifier = JWT.require(Algorithm.HMAC256(securityProperty.getAuthorizationSecret()))
                        .acceptLeeway(30)
                        .build();
                DecodedJWT jwt = verifier.verify(token);
                UserDetails user = getById(jwt.getSubject());
                SecurityContextHolder.getContext()
                        .setAuthentication(new UsernamePasswordAuthenticationToken(user, StringUtils.EMPTY, user.getAuthorities()));
            } catch (JWTVerificationException | UserDetailsNotFoundException ex) {
                log.warn("Invalid authorization {}", ex.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        chain.doFilter(req, resp);
    }

    abstract UserDetails getById(String principalId) throws UserDetailsNotFoundException;
}