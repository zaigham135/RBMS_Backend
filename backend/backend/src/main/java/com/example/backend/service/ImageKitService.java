package com.example.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

@Service
public class ImageKitService {

    private static final Logger log = LoggerFactory.getLogger(ImageKitService.class);
    private static final String UPLOAD_URL = "https://upload.imagekit.io/api/v1/files/upload";

    @Value("${imagekit.private-key}")
    private String privateKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String uploadProfilePhoto(MultipartFile file, String fileName) {
        try {
            // ImageKit uses HTTP Basic Auth with private key as username, empty password
            String auth = Base64.getEncoder().encodeToString((privateKey + ":").getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Basic " + auth);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("fileName", fileName);
            body.add("folder", "/profile-photos");
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(UPLOAD_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String url = (String) response.getBody().get("url");
                log.info("Photo uploaded to ImageKit: url={}", url);
                return url;
            }

            throw new RuntimeException("ImageKit upload failed: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to upload photo to ImageKit: {}", e.getMessage());
            throw new RuntimeException("Failed to upload photo: " + e.getMessage());
        }
    }
}
