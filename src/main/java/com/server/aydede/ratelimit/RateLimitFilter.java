package com.server.aydede.ratelimit;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        boolean isSessionEndpoint = "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().equals("/api/voice/session");
        return !isSessionEndpoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            // Auth yoksa Security zaten 401 döner; burada dokunma
            filterChain.doFilter(request, response);
            return;
        }

        String uid = auth.getPrincipal().toString();
        String ip = clientIp(request);

        var userResult = rateLimitService.tryConsume(uid);
        if (!userResult.allowed()) {
            log.warn("User rate limit exceeded: uid={}", uid);
            writeTooManyRequests(response, userResult.retryAfterSeconds());
            return;
        }

        var ipResult = rateLimitService.tryConsumeIp(ip);
        if (!ipResult.allowed()) {
            log.warn("IP rate limit exceeded: ip={}", ip);
            writeTooManyRequests(response, ipResult.retryAfterSeconds());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // "client, proxy1, proxy2" → ilk IP gerçek client
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response, long retryAfterSeconds)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"message\":\"Too many requests\",\"code\":\"RATE_LIMITED\"}");
    }
}