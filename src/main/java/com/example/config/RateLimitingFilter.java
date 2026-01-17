package com.example.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${rate-limit.requests-per-window:100}")
    private int requestsPerWindow;

    @Value("${rate-limit.window-size-ms:60000}")
    private long windowSizeMs;

    private final ConcurrentHashMap<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();
    private long lastCleanup = System.currentTimeMillis();
    private static final long CLEANUP_INTERVAL_MS = 60000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (requestsPerWindow <= 0)
            requestsPerWindow = 100;
        if (windowSizeMs <= 0)
            windowSizeMs = 60000;

        var clientIp = getClientIp(request);
        cleanupIfNeeded();

        var bucket = buckets.computeIfAbsent(clientIp,
                ip -> new RateLimitBucket(windowSizeMs, requestsPerWindow));

        if (bucket.tryConsume()) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerWindow));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(bucket.getRemaining()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(bucket.getResetTime()));
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerWindow));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", String.valueOf(bucket.getResetTime()));
            response.setHeader("Retry-After",
                    String.valueOf((bucket.getResetTime() - System.currentTimeMillis()) / 1000));
            response.getWriter().write("{\"error\": \"Rate limit exceeded. Please try again later.\"}");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        var xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void cleanupIfNeeded() {
        var now = System.currentTimeMillis();
        if (now - lastCleanup > CLEANUP_INTERVAL_MS) {
            lastCleanup = now;
            buckets.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        var path = request.getRequestURI();
        return path.equals("/health") || path.startsWith("/actuator/");
    }

    private static class RateLimitBucket {
        private final long windowSizeMs;
        private final int maxRequests;
        private final AtomicInteger requestCount;
        private final AtomicLong windowStart;

        public RateLimitBucket(long windowSizeMs, int maxRequests) {
            this.windowSizeMs = windowSizeMs;
            this.maxRequests = maxRequests;
            this.requestCount = new AtomicInteger(0);
            this.windowStart = new AtomicLong(System.currentTimeMillis());
        }

        public synchronized boolean tryConsume() {
            var now = System.currentTimeMillis();
            if (now - windowStart.get() >= windowSizeMs) {
                windowStart.set(now);
                requestCount.set(0);
            }
            if (requestCount.get() < maxRequests) {
                requestCount.incrementAndGet();
                return true;
            }
            return false;
        }

        public int getRemaining() {
            return Math.max(0, maxRequests - requestCount.get());
        }

        public long getResetTime() {
            return windowStart.get() + windowSizeMs;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - windowStart.get() > windowSizeMs * 2;
        }
    }
}
