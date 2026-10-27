# Bring-Your-Own-Key (BYOK) Architecture for StudyGem (Approved)

Implementation plan for StudyGem's client-side Bring-Your-Own-Key (BYOK) architecture with dedicated Onboarding, real-time Gemini verification, and AES256_GCM EncryptedSharedPreferences.

### Technical Specifications & Refinements

- **Dependencies**: `androidx.security:security-crypto:1.1.0` (or compatible stable artifact) in `libs.versions.toml` & `app/build.gradle.kts`.
- **Fault-Tolerant Secure Storage**: `SecurePreferencesManager` with `MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()` and `EncryptedSharedPreferences`. Wrapped in `try-catch` for `GeneralSecurityException` and `IOException`; auto-resets corrupted KeyStore state rather than crashing.
- **Verification Ping (`verifyApiKey`)**: Lightweight query (`maxOutputTokens = 1`) to Gemini Flash 3.8 (`gemini-3.8-flash` with `gemini-3.5-flash` fallback). Distinguishes network issues from HTTP 400/403 auth rejections.
- **Input Sanitization**: Automatically strips leading/trailing spaces, newlines, tabs, and quotes.
- **Dedicated Onboarding View**: Educates on free key usage, button linking to `https://aistudio.google.com/app/apikey`, interactive validation, and seamless transition to dashboard.
- **Purge Secrets**: Complete removal of `BuildConfig.GEMINI_API_KEY` and clean `.env.example`.
