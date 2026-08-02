package com.example.backend.config;

import com.example.backend.Entities.User;
import com.example.backend.enums.Role;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @org.springframework.beans.factory.annotation.Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("OAuth2SuccessHandler initialized with frontendUrl={}", frontendUrl);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String email;
        String name;
        String googleId;

        Object principal = authentication.getPrincipal();

        if (principal instanceof OidcUser oidcUser) {
            email = oidcUser.getEmail();
            name = oidcUser.getFullName();
            googleId = oidcUser.getSubject();
        } else if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name");
            googleId = oauth2User.getAttribute("sub");
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unknown OAuth2 principal");
            return;
        }

        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not provided by Google");
            return;
        }

        // Find or create user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name != null ? name : email);
            newUser.setGoogleId(googleId);
            newUser.setRole(Role.EMPLOYEE);
            newUser.setStatus("ACTIVE");
            newUser.setPassword("GOOGLE_OAUTH_NO_PASSWORD");
            // save Google profile picture URL directly
            if (principal instanceof OidcUser oidcUser && oidcUser.getPicture() != null) {
                newUser.setProfilePhoto(oidcUser.getPicture());
            }
            log.info("Creating new user from Google OAuth: email={}", email);
            return userRepository.save(newUser);
        });

        // Update googleId if missing
        if (user.getGoogleId() == null && googleId != null) {
            user.setGoogleId(googleId);
            userRepository.save(user);
        }

        if ("INACTIVE".equals(user.getStatus())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Account deactivated");
            return;
        }

        String token = jwtUtil.generateToken(user.getEmail());
        log.info("Google OAuth login successful: email={}, role={}", email, user.getRole());

        // Redirect to frontend with token
        String redirectUrl = frontendUrl + "/auth/callback"
                + "?token=" + token
                + "&role=" + user.getRole().name()
                + "&name=" + java.net.URLEncoder.encode(user.getName(), "UTF-8")
                + "&email=" + java.net.URLEncoder.encode(user.getEmail(), "UTF-8")
                + "&userId=" + user.getId()
                + "&profilePhoto=" + java.net.URLEncoder.encode(user.getProfilePhoto() != null ? user.getProfilePhoto() : "", "UTF-8");

        response.sendRedirect(redirectUrl);
    }
}
