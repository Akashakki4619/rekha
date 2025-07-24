package com.example.ratelimitedapi.aspect;

import com.example.ratelimitedapi.annotation.RateLimit;
import com.example.ratelimitedapi.exception.RateLimitExceededException;
import com.example.ratelimitedapi.service.RateLimitingService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RateLimitingAspect {

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private HttpServletRequest request;

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = getClientKey(rateLimit.key());
        
        if (rateLimitingService.tryConsume(key)) {
            return joinPoint.proceed();
        } else {
            throw new RateLimitExceededException("Rate limit exceeded. Try again later.");
        }
    }

    private String getClientKey(String key) {
        // Use client IP address as the key for rate limiting
        String clientIp = getClientIpAddress();
        return key + ":" + clientIp;
    }

    private String getClientIpAddress() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}