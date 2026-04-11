package com.example.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class OAuthCallbackRedirectController {

    @GetMapping("/auth/callback")
    public void redirectToFrontendCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String query = request.getQueryString();
        String redirectUrl = "http://localhost:3000/auth/callback" + (query != null ? "?" + query : "");
        response.sendRedirect(redirectUrl);
    }
}
