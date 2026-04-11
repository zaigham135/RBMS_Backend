package com.example.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;

@Service
public class ImageKitService {

    private static final Logger log = LoggerFactory.getLogger(ImageKitService.class);
    private static final String UPLOAD_URL = "https://upload.imagekit.io/api/v1/files/upload";

    @Value("${imagekit.private-key}")
    private String privateKey;

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);
        return new RestTemplate(factory);
    }

    public String uploadProfilePhoto(MultipartFile file, String fileName) {
        try {
            log.info("Uploading photo to ImageKit: fileName={}, originalSize={} bytes", fileName, file.getSize());

            // Compress and resize image before uploading
            byte[] compressedBytes = compressImage(file);
            log.info("Compressed image size: {} bytes", compressedBytes.length);

            String auth = Base64.getEncoder().encodeToString((privateKey + ":").getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Basic " + auth);

            // Use .jpg extension for compressed output
            String compressedFileName = fileName.replaceAll("\\.[^.]+$", "") + ".jpg";

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("fileName", compressedFileName);
            body.add("folder", "/profile-photos");
            body.add("file", new ByteArrayResource(compressedBytes) {
                @Override
                public String getFilename() {
                    return compressedFileName;
                }
            });

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = buildRestTemplate().postForEntity(UPLOAD_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String url = (String) response.getBody().get("url");
                log.info("Photo uploaded successfully: url={}", url);
                return url;
            }

            throw new RuntimeException("ImageKit upload failed with status: " + response.getStatusCode());

        } catch (HttpClientErrorException e) {
            log.error("ImageKit API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("ImageKit upload failed: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Failed to upload photo: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload photo: " + e.getMessage());
        }
    }

    private byte[] compressImage(MultipartFile file) throws Exception {
        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) throw new RuntimeException("Invalid image file");

        // Resize to max 800x800 maintaining aspect ratio
        int maxDimension = 800;
        int width = original.getWidth();
        int height = original.getHeight();

        if (width > maxDimension || height > maxDimension) {
            double scale = Math.min((double) maxDimension / width, (double) maxDimension / height);
            width = (int) (width * scale);
            height = (int) (height * scale);
        }

        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();

        // Compress to JPEG at 80% quality
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        javax.imageio.ImageWriter writer = javax.imageio.ImageIO.getImageWritersByFormatName("jpeg").next();
        javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.8f);
        writer.setOutput(javax.imageio.ImageIO.createImageOutputStream(out));
        writer.write(null, new javax.imageio.IIOImage(resized, null, null), param);
        writer.dispose();

        return out.toByteArray();
    }
}
