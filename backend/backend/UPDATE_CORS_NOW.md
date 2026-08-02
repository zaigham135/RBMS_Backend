# Update Backend CORS for Vercel - Quick Guide

## Your Vercel URL
After deployment, you'll get a URL like:
`https://rbms-front-end-dep.vercel.app`

## Update SecurityConfig.java

**File**: `Backend_Java/backend/backend/src/main/java/com/example/backend/config/SecurityConfig.java`

**Find this method** (around line 28):
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

**Replace with** (add your Vercel URL):
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(
        "http://localhost:3000",
        "https://rbms-front-end-dep.vercel.app"  // Replace with your actual Vercel URL
    ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

## Rebuild and Redeploy Backend

```powershell
# Navigate to backend
cd "E:\role based task  management project\Backend_Java\backend\backend"

# Build
mvn clean package -DskipTests

# Copy JAR to deployment folder
Copy-Item target/*.jar eb-deploy-temp/application.jar -Force

# Deploy to AWS
cd eb-deploy-temp
eb deploy
```

## Verify CORS is Working

1. Open your Vercel URL in browser
2. Open Developer Console (F12)
3. Try logging in
4. Check for CORS errors in console
5. If no errors, you're good! ✅

## Alternative: Use Wildcard for All Vercel Domains

If you want to allow all Vercel preview deployments:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of(
        "http://localhost:3000",
        "https://*.vercel.app"  // Allows all Vercel domains
    ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

Note: Use `setAllowedOriginPatterns` instead of `setAllowedOrigins` for wildcards.

## Done!

Your application should now work:
- Frontend: https://rbms-front-end-dep.vercel.app
- Backend: http://taskmanagement-backend-prod.eba-bdtdzgpg.eu-north-1.elasticbeanstalk.com
