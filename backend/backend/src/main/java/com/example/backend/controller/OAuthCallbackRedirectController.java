package com.example.backend.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class OAuthCallbackRedirectController {

    private static final Logger log = LoggerFactory.getLogger(OAuthCallbackRedirectController.class);

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        log.info("OAuthCallbackRedirectController initialized with frontendUrl={}", frontendUrl);
    }

    @GetMapping("/auth/callback")
    public void redirectToFrontendCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String query = request.getQueryString();
        String redirectUrl = frontendUrl + "/auth/callback" + (query != null ? "?" + query : "");
        log.info("Redirecting OAuth callback to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
