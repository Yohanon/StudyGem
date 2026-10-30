# StudyGem Architecture & Explain Toggle Feature

Implementation specification for StudyGem's client-side Bring-Your-Own-Key (BYOK) architecture, multimodal vision, and the Explain Mode toggle.

### 1. Explain Toggle Architecture (Replaced Learn & Demonstrate Modes)
- **Scrapped Modes**: Deprecated separate "Learn" and "Demonstrate" modes in favor of a single unified **Explain** toggle.
- **Toggle OFF (Default / Short Mode)**:
  - Answers are strictly limited to at most 1 to 2 paragraphs (under 150 words).
  - Model delivers immediate, core answers with zero filler, no greetings, and no unnecessary elaborations.
  - Token budget reduced to 450 tokens to enforce conciseness.
- **Toggle ON (Focused Explanation Mode)**:
  - Comprehensive explanation of academic concepts, code, math formulas, or diagrams.
  - Strict guardrail: All unnecessary data, pleasantries, and tangential fluff are cut out; model stays strictly on the academic subject.
  - Generative budget adjusted to 2048 tokens.
- **UI & UX**:
  - `ExplainToggle` component with M3 card styling, active status pill (`ON` / `OFF`), explanatory helper text, and a Material 3 `Switch` with 48dp touch targets.
  - Real-time persistence via `SecurePreferencesManager`.

### 2. Client-Side Bring-Your-Own-Key (BYOK)
- **Secure Storage**: `SecurePreferencesManager` using `MasterKey` (AES256_GCM) and `EncryptedSharedPreferences`. Auto-resets corrupted KeyStore state rather than crashing.
- **Verification Ping (`verifyApiKey`)**: Validates against Google AI Studio API directly (with models list auth gateway and ping fallback).
- **Input Sanitization**: Automatically strips whitespace, tabs, newlines, and surrounding quotes.
- **Settings & Management**: Full control to test, mask/reveal, update, or clear keys at any time.
