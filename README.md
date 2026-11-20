# AI Assistant India — Complete Production Android Application

**AI Assistant India** is a full-stack Kotlin & Jetpack Compose Android application built with Material 3, MVVM + Clean Architecture, Firebase Authentication (Google Credential Manager + Phone OTP), Cloud Firestore, Firebase AI Logic (`Firebase.ai`), Google AdMob, Google Play Billing, and a companion Firebase Cloud Functions + Web Admin Console.

## Architecture & Modules

- **Android App (`/app`)**
  - **UI / Theme**: Jetpack Compose, Material 3, Dark Purple (`#160D2B`) & Premium Gold (`#FFD166`) branding, Light & Dark theme toggle, `Space Grotesk` / `Plus Jakarta Sans` / `JetBrains Mono` typography, responsive `NavigationBar` / `NavigationRail` adaptive shell.
  - **15 Functional AI Features** (powered by Firebase AI Logic `gemini-3.8-flash` & `gemini-3.5-flash-lite` with App Check):
    1. **AI Chat Assistant** (`ChatScreen`) — Streaming responses, persistent Firestore history, New Chat, Stop Generation, Regenerate, Copy, Share, Delete, Hindi/English support.
    2. **AI Image Analyzer** (`ImageAnalyzerScreen`) — Android Photo Picker + Camera capture (`CAMERA` permission), Gemini Vision Q&A, OCR & educational breakdown.
    3. **AI Voice Assistant** (`VoiceAssistantScreen`) — `RECORD_AUDIO` permission, Android `SpeechRecognizer` & `TextToSpeech` (Hindi `hi-IN` & English `en-IN`), stop playback.
    4. **AI Translator** (`TranslatorScreen`) — 50+ languages, Auto-Detect, Swap, Copy, TTS pronunciation.
    5. **AI Content Writer** (`ContentWriterScreen`) — YouTube titles/descriptions, hashtags, Instagram/Facebook posts, blogs, product copy.
    6. **AI Homework Helper** (`HomeworkHelperScreen`) — Math, Physics, Chemistry, Biology, English, GK, NEET preparation + photo question upload.
    7. **AI Email & Application Writer** (`EmailWriterScreen`) — Leave, School, College, Government, Job, Formal Letters + editable draft & sharing.
    8. **AI Story Generator** (`StoryGeneratorScreen`) — 8 genres, length/style selectors, TTS audio playback.
    9. **AI Coding Assistant** (`CodingAssistantScreen`) — Kotlin, Java, Python, HTML, CSS, JS, C, C++ code generation, debugging & explanations.
    10. **AI Resume Maker** (`ResumeMakerScreen`) — Executive/Tech/Fresher templates + real Android `PdfDocument` export & sharing via `FileProvider`.
    11. **AI PDF Assistant** (`PdfAssistantScreen`) — SAF PDF selection (10 MB limit), `PdfRenderer` + Gemini Vision summary/notes/Q&A, Firestore file history.
    12. **YouTube Creator AI** (`YouTubeCreatorScreen`) — 9 creator workflows including scripts, SEO, thumbnail prompts, 30-day planner.
    13. **Business AI** (`BusinessAiScreen`) — Startup ideas, marketing copy, customer templates, business plans.
    14. **AI Text Summarizer** (`TextSummarizerScreen`) — Short/detailed summaries, bullets, rewriting, grammar correction.
    15. **AI Daily Planner** (`DailyPlannerScreen`) — Structured JSON AI timetable generation (`Schema.obj`), Firestore task CRUD, completion tracking, local reminder notifications (`POST_NOTIFICATIONS`).
- **Firebase Security Rules (`/firestore.rules`, `/storage.rules`, `/firebase-blueprint.json`, `/firestore.test.js`)**
  - Zero-trust per-user ownership rules (`uid` enforcement), named Firestore database `ai-studio-android-aiassist-dc68094c-1594-4e78-955f-bbb46d360a8a`, verified admin bootstrap for `biharkrantidal76@gmail.com`.
- **Cloud Functions (`/functions`)**: Server-side credit validation, Play Billing verification hook, admin claims & FCM notifications.
- **Web Admin Console (`/admin`)**: Real-time analytics dashboard, user & credit management, `appConfig/global` model/cost editor, maintenance mode, and notification sender.

---

## Android Studio Build & Setup

1. Open the project root in **Android Studio** (AGP 9.x / JDK 11+).
2. Do **not** create `local.properties` manually if building in CI/cloud; Android Studio configures `sdk.dir` automatically on local machines.
3. No Gemini API key is needed in `.env` or `BuildConfig` — all Gemini calls execute via **Firebase AI Logic** (`Firebase.ai`) authenticated by **Firebase App Check**.
4. Build and install debug APK:
   ```bash
   gradle :app:assembleDebug
   ```
5. Run local JVM & Firestore security rules tests:
   ```bash
   gradle :app:testDebugUnitTest
   node --test firestore.test.js
   ```

---

## Firebase Console & Play Console Enablement Checklist

AI Studio automatically provisioned Firestore (`ai-studio-android-aiassist-dc68094c-1594-4e78-955f-bbb46d360a8a`), Firebase AI Logic, App Check, and Google Sign-In for project `gen-lang-client-0466207256`. Before public Play Store release, verify these optional console items:

1. **Phone Number (OTP) Authentication**:
   - Firebase Console → **Authentication** → **Sign-in method** → Enable **Phone**.
   - Ensure your Play App Signing SHA-1 & SHA-256 certificates are added under **Project Settings → Your Apps**.
2. **Firebase Storage Rules**:
   - Firebase Console → **Storage** → Initialize Storage bucket and deploy `/storage.rules`:
     ```bash
     firebase deploy --only storage
     ```
3. **Firebase Cloud Messaging (FCM)**:
   - Enabled automatically via `google-services.json`. Create topic `all_users` or send test push notifications via Cloud Messaging Console.
4. **Cloud Functions (`/functions`)**:
   - Upgrade project to **Blaze** plan, configure secrets in `/functions/.env`, and deploy:
     ```bash
     cd functions && npm install && firebase deploy --only functions
     ```
5. **Google AdMob Production IDs**:
   - The app currently uses official **Google AdMob Test Ad Unit IDs** in `AndroidManifest.xml` and `AdMobManager.kt`.
   - Before release, replace the manifest `APPLICATION_ID` meta-data and `AdMobIds` constants with your verified AdMob App ID and Banner / Native / Rewarded Ad Unit IDs.
6. **Google Play Billing Subscriptions**:
   - In **Google Play Console → Monetize → Subscriptions**, create subscription products with exact IDs:
     - `ai_india_weekly` (Weekly Premium)
     - `ai_india_monthly` (Monthly Premium)
     - `ai_india_yearly` (Yearly Premium)
7. **Web Admin Panel (`/admin`)**:
   - Register a **Web App** inside Firebase Console → Project Settings and paste its `apiKey` and `appId` into `/admin/app.js`.
   - Host `/admin` locally or on Firebase Hosting (`firebase deploy --only hosting`).
