package com.trustamarket.walletservice.wallet.global.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SlowRequestLoggingFilter extends OncePerRequestFilter {

    private static final long THRESHOLD_MS = 5000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            if (duration >= THRESHOLD_MS) {
                log.warn("[SLOW] {} {} - {}ms", request.getMethod(), request.getRequestURI(), duration);
            }
        }
    }
}
