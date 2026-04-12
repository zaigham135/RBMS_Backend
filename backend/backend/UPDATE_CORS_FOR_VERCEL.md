# Update Backend CORS for Vercel

After deploying your frontend to Vercel, you'll get a URL like:
`https://your-project-name.vercel.app`

## Step 1: Update SecurityConfig.java

File: `Backend_Java/backend/backend/src/main/java/com/example/backend/config/SecurityConfig.java`

Find the `corsConfigurationSource()` method and update it:

### Current Code:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

### Updated Code:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(
        "http://localhost:3000",                    // For local development
        "https://your-project-name.vercel.app"      // Replace with your actual Vercel URL
    ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

## Step 2: Rebuild and Redeploy Backend

```powershell
# Navigate to deployment directory
cd "E:\role based task  management project\Backend_Java\backend\backend\eb-deploy-temp"

# Build the application
cd ..
mvn clean package -DskipTests

# Copy the JAR to deployment directory
Copy-Item target/*.jar eb-deploy-temp/application.jar

# Deploy to AWS
cd eb-deploy-temp
eb deploy
```

## Step 3: Verify CORS is Working

After redeployment, test from your Vercel frontend:
1. Open browser console (F12)
2. Try logging in
3. Check for CORS errors
4. If you see CORS errors, verify the Vercel URL matches exactly

## Alternative: Use Environment Variable for CORS

For better flexibility, you can use an environment variable:

### Update SecurityConfig.java:
```java
@Value("${FRONTEND_URL:http://localhost:3000}")
private String frontendUrl;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(
        "http://localhost:3000",
        frontendUrl
    ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

### Add to set-env-eu-north.ps1:
```powershell
eb setenv FRONTEND_URL=https://your-project-name.vercel.app
```

This way, you can update the frontend URL without rebuilding the backend!

## Troubleshooting

### CORS Error Still Appears
1. Verify the Vercel URL is exactly correct (no trailing slash)
2. Check if backend redeployed successfully: `eb status`
3. Check backend logs: `eb logs`
4. Clear browser cache and try again

### Multiple Vercel URLs
Vercel gives you:
- Production URL: `https://your-project-name.vercel.app`
- Preview URLs: `https://your-project-name-git-branch.vercel.app`

You may want to allow all Vercel domains:
```java
config.setAllowedOriginPatterns(List.of(
    "http://localhost:3000",
    "https://*.vercel.app"
));
```

Note: Use `setAllowedOriginPatterns` instead of `setAllowedOrigins` for wildcards.
