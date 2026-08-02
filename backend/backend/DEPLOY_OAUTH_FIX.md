# Deploy OAuth Redirect Fix to AWS

## Changes Made
1. Updated `OAuth2SuccessHandler.java` to use configurable frontend URL
2. Added `FRONTEND_URL` environment variable to `application.properties`
3. Updated `set-env-eu-north.ps1` to include `FRONTEND_URL`

## Deployment Steps

### Step 1: Set Environment Variable
```powershell
cd Backend_Java/backend/backend/eb-deploy-temp
./set-env-eu-north.ps1
```

This will add `FRONTEND_URL=https://rbms-frontend-next-js.vercel.app` to your Elastic Beanstalk environment.

### Step 2: Build and Deploy
```powershell
cd Backend_Java/backend/backend
mvn clean package -DskipTests
cd eb-deploy-temp
eb deploy
```

### Step 3: Verify Deployment
```powershell
eb health
```

Wait for the environment to show "Ok" status.

### Step 4: Update Google OAuth Console

Go to [Google Cloud Console](https://console.cloud.google.com/apis/credentials):

1. Select your OAuth 2.0 Client ID
2. Add these Authorized redirect URIs:
   - `http://taskmanagement-backend-prod.eba-bdtdzgpg.eu-north-1.elasticbeanstalk.com/login/oauth2/code/google`
   - `http://localhost:8080/login/oauth2/code/google` (for local testing)

3. Add these Authorized JavaScript origins (if needed):
   - `https://rbms-frontend-next-js.vercel.app`
   - `http://localhost:3000`

## Testing

### Test Google OAuth Flow:
1. Go to https://rbms-frontend-next-js.vercel.app
2. Click "Sign in with Google"
3. Complete Google authentication
4. Should redirect back to your Vercel app with token

### Test Local Development:
1. Ensure `.env.local` has: `NEXT_PUBLIC_API_URL=http://localhost:8080`
2. Start local backend: `mvn spring-boot:run`
3. Start frontend: `npm run dev`
4. Test login at http://localhost:3000

## Environment Variables Summary

### Backend (AWS Elastic Beanstalk):
- `FRONTEND_URL=https://rbms-frontend-next-js.vercel.app`

### Frontend (Vercel):
- `NEXT_PUBLIC_API_URL=http://taskmanagement-backend-prod.eba-bdtdzgpg.eu-north-1.elasticbeanstalk.com`

### Frontend (Local `.env.local`):
- `NEXT_PUBLIC_API_URL=http://localhost:8080` (for local backend)
- OR `NEXT_PUBLIC_API_URL=http://taskmanagement-backend-prod.eba-bdtdzgpg.eu-north-1.elasticbeanstalk.com` (for AWS backend)
