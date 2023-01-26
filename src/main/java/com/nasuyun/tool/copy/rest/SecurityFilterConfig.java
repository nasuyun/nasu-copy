package com.nasuyun.tool.copy.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.security.auth.login.CredentialExpiredException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SecurityFilterConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityFilterConfig.class);

    @Autowired
    SessionProvider sessionProvider;

    /**
     * 访问API session设置
     * @return
     */
    @Bean
    public FilterRegistrationBean apiSecurityFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new APISecurityFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("apiSecurityFilter");
        registration.setOrder(1);
        return registration;
    }

    class APISecurityFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            log.debug("[{}] - {} : {}", request.getMethod(), request.getRequestURI());
            String sessionKey = request.getHeader("sessionKey");
            String username = request.getHeader("username");
            try {
                sessionProvider.setUserCredential(sessionKey, username, "admin");
            } catch (CredentialExpiredException e) {
            }

            filterChain.doFilter(request, response);
            sessionProvider.clearSession();
        }
    }
}