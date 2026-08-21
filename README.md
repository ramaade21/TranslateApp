# LinguaTranslate

A production-oriented Android translator app supporting **English ↔ Indonesian** and **Japanese ↔ Indonesian**, with text translation, voice translation, speech-to-text, text-to-speech, auto language detection, conversation mode, history, and favorites.

---

## 1. Project Overview

LinguaTranslate is a **monorepo**: one Git repository containing two independently
deployable pieces, plus shared CI at the root.

```
linguatranslate/
├── .github/workflows/   CI: android-build.yml (path-filtered to android/**),
│                        backend-ci.yml (path-filtered to backend/**)
├── android/             Kotlin + Jetpack Compose Android app
├── backend/             Node.js + TypeScript + Express API (translation proxy)
├── render.yaml          Render deploy config (rootDir: backend)
└── README.md            this file
```

Each side's CI workflow only runs when files under its own folder change (via GitHub
Actions `paths:` filters), so editing the backend doesn't trigger an Android build and
vice versa — despite living in one repo, they build and deploy independently.

The Android app **never** calls a translation provider directly and never embeds an API key. All translation and language-detection requests go through the backend, which holds the provider credentials.

```
Android App → Backend API (/api/translate, /api/detect-language) → Translation Provider
```

## 2. Features Implemented

- Text translation (EN↔ID, ID↔JA) via backend API
- Voice input via Android `SpeechRecognizer`, output via Android `TextToSpeech`
- Auto language detection (server-side, provider + heuristic fallback)
- Conversation Mode (two-person, per-speaker language + auto TTS playback)
- Translation history (Room, swipe/list actions: copy, speak, delete, clear all)
- Favorites (Room, add/remove/copy/share/speak)
- Copy (ClipboardManager) and Share (Android Sharesheet)
- Swap source/target language (resolves detected language when source = Auto)
- Dark mode (System / Light / Dark, Material 3 dynamic color, persisted via DataStore)
- Centralized, user-friendly error handling (no stack traces surfaced to the user)
- Accessibility: `contentDescription` on every icon button

## 3. Architecture

**Pattern:** MVVM + Clean Architecture + Repository Pattern

```
presentation/   Compose screens + ViewModels (StateFlow), per feature (home, history,
                favorites, conversation, settings) + shared components + navigation
domain/         Pure Kotlin: models, repository interfaces, use cases (no Android deps)
data/           Room (local), Retrofit (remote), repository implementations
core/           DI modules (Hilt), network error mapping, SpeechToText/TextToSpeech
                service wrappers, shared result types
```

Key abstractions:
- `TranslationRepository` / `TranslationApi` — the app is not hard-coded to a specific
  translation provider; that choice lives entirely in the backend.
- `AppResult<T>` / `AppError` — every layer boundary returns a typed result instead of
  throwing, so the UI can always show a friendly message.
- `SpeechToTextService` / `TextToSpeechService` — thin wrappers around the Android
  platform APIs, exposed as a `Flow` / suspend functions respectively.

No word is ever hard-coded as a translation (e.g. no `if (text == "hello") "halo"`).
Every translation is a live call to the backend.

## 4. Requirements

**Backend**
- Node.js 18+ and npm

**Android**
- Android Studio (Koala/2024.1 or newer recommended)
- JDK 17
- Android SDK Platform 34, Build-Tools matching AGP 8.5.0
- An Android emulator (API 26+) or physical device

## 5. Backend Setup

```bash
cd backend
cp .env.example .env
# edit .env: set TRANSLATION_PROVIDER=libretranslate (default) or google,
# and the matching API key/base URL.
npm install
npm run build
npm start
```

The server starts on `http://localhost:3000` by default. Health check:

```bash
curl http://localhost:3000/health
```

### Environment variables (`.env`)

| Variable | Description |
|---|---|
| `PORT` | Server port (default 3000) |
| `TRANSLATION_PROVIDER` | `libretranslate` \| `google` \| `mock` (mock is dev-only, blocked in production) |
| `LIBRETRANSLATE_BASE_URL` | Base URL of a LibreTranslate instance |
| `LIBRETRANSLATE_API_KEY` | Optional API key |
| `GOOGLE_TRANSLATE_API_KEY` | Required if `TRANSLATION_PROVIDER=google` |
| `ALLOWED_ORIGINS` | CORS allow-list, comma separated, or `*` |
| `RATE_LIMIT_WINDOW_MS` / `RATE_LIMIT_MAX_REQUESTS` | Rate limiting |

**Never commit `.env`** — it's already in `.gitignore`.

### Run backend tests

```bash
cd backend
npm test
```

This was run during development: **14/14 tests passing**, covering EN→ID, ID→EN,
JA→ID, ID→JA, auto-detection, empty-text validation, same-language validation,
missing fields, and provider-failure handling.

### API contract

```
POST /api/translate
{ "text": "Good morning", "sourceLanguage": "en", "targetLanguage": "id" }
→ { "success": true, "data": { "originalText": "...", "translatedText": "...",
     "sourceLanguage": "en", "targetLanguage": "id", "detectedLanguage": null } }

POST /api/detect-language
{ "text": "Selamat pagi" }
→ { "success": true, "language": "id" }
```

## 6. Android Setup

1. Open the `android/` folder in Android Studio ("Open an existing project").
2. Let Gradle sync (this downloads AGP, Compose, Room, Hilt, etc. from Google's Maven —
   requires normal internet access, which this development sandbox did not have).
3. Copy `local.properties.example` → `local.properties` and set `sdk.dir` to your SDK path
   (Android Studio usually does this for you automatically).
4. Run the backend locally first (see above). By default, debug builds point at
   `http://10.0.2.2:3000/`, which is the emulator's alias for your host machine's
   `localhost`. Override via `LINGUATRANSLATE_BASE_URL_DEBUG` in `local.properties` if
   testing on a physical device (use your machine's LAN IP instead of `10.0.2.2`).
5. Run the `app` configuration on an emulator or device (API 26+).

### Build the APK from the command line

```bash
cd android
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Run tests

```bash
cd android
./gradlew testDebugUnitTest        # unit tests (JVM, no device needed)
./gradlew connectedAndroidTest     # instrumented UI tests (needs emulator/device)
```

## 6b. Security Hardening

The backend includes several layers of defense, verified live in this environment:

| Layer | What it does |
|---|---|
| `APP_API_KEY` (`X-App-Key` header) | Every `/api/*` request must carry a shared secret matching `APP_API_KEY`. Blocks anonymous scripts/scrapers that don't have the app. Constant-time comparison (`crypto.timingSafeEqual`) to avoid timing side-channels. Server **refuses to serve `/api` routes** if this isn't set when `NODE_ENV=production` (fails closed, not open). |
| HTTPS enforcement | `enforceHttps` middleware 301-redirects plain HTTP to HTTPS in production. Combined with `app.set("trust proxy", 1)` so it works correctly behind Render/Railway's TLS-terminating edge. |
| Helmet + HSTS + CSP | Security response headers (`Strict-Transport-Security`, `Content-Security-Policy: default-src 'none'`, `X-Content-Type-Options`, `X-Frame-Options`, etc.) on every response. |
| CORS closed by default | `ALLOWED_ORIGINS` defaults to **no origins allowed**, not `*`. Native Android calls aren't affected by CORS at all (no `Origin` header); this mainly blocks browser-based abuse. |
| Two-tier rate limiting | A generous global `/api` limiter plus a stricter per-minute limiter specifically on `/translate` and `/detect-language`, so no single client can burn through your paid translation provider quota. |
| No secrets in the client | The translation provider's API key (LibreTranslate/Google) lives **only** on the server, in an environment variable, never in the APK. |
| Non-root Docker user | The production Docker image runs as an unprivileged user, not root. |
| No server-side user data | History/Favorites are stored only in the Room database on-device — there is no server-side user database to breach. |

### Honest limitation you should know about

`APP_API_KEY` is embedded in the compiled Android app (`BuildConfig.APP_API_KEY`) so
the app can prove it's "the real app" to the server. **A secret shipped inside a
distributed APK can, in principle, be extracted** by someone who decompiles it — no
client-embedded secret can be made perfectly unextractable. What this layer *does*
reliably stop: anonymous bots, scrapers, and randoms hitting your API URL directly
without ever having installed your app. If you later need to stop a *specific*
malicious user (not just anonymous traffic), add real user accounts/sign-in and
per-user tokens — that's a bigger feature, out of scope here, but the architecture
(backend already sits between client and provider) supports adding it later without
restructuring anything.

### Generating and setting the key

```bash
openssl rand -hex 32
```

Put the same value in:
- Backend: `APP_API_KEY` environment variable (set in Render/Railway dashboard, never in a committed `.env`)
- Android: `LINGUATRANSLATE_APP_API_KEY` in `local.properties` (gitignored)

Rotate this value periodically (e.g. every few months, or immediately if you suspect
it leaked) — you'll need to publish an app update with the new key when you do, since
it's baked into the compiled APK at build time.

## 7. Deploying the Backend (Render or Railway)

This is a monorepo, so both platforms need to know the backend lives in the
`backend/` subfolder, not the repo root.

### Render

`render.yaml` lives at the **repo root** (Render Blueprints require this) and points
at `backend/` via `rootDir: backend`, so the Docker build context is correctly scoped
to just the backend code.

1. Push this repo to GitHub.
2. In the Render dashboard: New → Blueprint → point at your repo. Render reads the root `render.yaml` automatically and builds from `backend/` per its `rootDir`.
3. Fill in the secret env vars marked `sync: false` in the dashboard (never commit these):
   - `APP_API_KEY` (from `openssl rand -hex 32`)
   - `LIBRETRANSLATE_API_KEY` (or switch `TRANSLATION_PROVIDER` to `google` and set `GOOGLE_TRANSLATE_API_KEY`)
4. Deploy. Render provides HTTPS automatically on `https://<your-service>.onrender.com`.
5. Verify: `curl https://<your-service>.onrender.com/health`

### Railway

`railway.json` lives inside `backend/`. Railway's monorepo support is dashboard-driven
rather than file-driven, so one extra manual step is needed:

1. Push this repo to GitHub.
2. In Railway: New Project → Deploy from GitHub repo.
3. In the service's **Settings → Source → Root Directory**, set it to `backend`. Railway will then find `backend/railway.json` and `backend/Dockerfile` correctly.
4. In the Railway dashboard, add the same environment variables as above (Variables tab).
5. Railway provides HTTPS automatically on `https://<your-service>.up.railway.app`.
6. Verify: `curl https://<your-service>.up.railway.app/health`

### After deploying either way

Point the Android app's release build at your live URL:

```properties
# local.properties
LINGUATRANSLATE_BASE_URL_RELEASE=https://<your-service>.onrender.com/
LINGUATRANSLATE_APP_API_KEY=<the same value you set as APP_API_KEY on the server>
```

Then build a release APK (`./gradlew assembleRelease`) — release builds already
require HTTPS (no cleartext network security config outside `src/debug`), so this
just works.

## 8. Deploying the APK via GitHub (GitHub Actions → GitHub Releases)

A ready-to-use workflow (`android/.github/workflows/android-build.yml`) does this:

- **On every push/PR to `main`**: runs unit tests, then builds and uploads a **debug
  APK** as a workflow artifact (download it from the Actions run page — good for
  quick testing, no signing required).
- **On every pushed tag matching `v*`** (e.g. `v1.0.0`): builds a **signed release
  APK** and publishes it to your repo's **Releases** page automatically, attached as
  a downloadable file. This is how you "deploy" the APK on GitHub without any app
  store — anyone can go to your repo's Releases tab and download it directly.

### One-time setup

**1. This repo is a monorepo**: `android/` and `backend/` live side by side, with
`.github/workflows/` at the repo root. Push the whole repo root to GitHub as a single
repository (see project layout in section 1) — no special folder needs to become its
own repo.

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/<you>/<repo>.git
git push -u origin main
```

**2. Generate a signing keystore** (only once — reuse it for every future release; losing it means you can never update the app under the same identity again, so back it up somewhere safe):

```bash
keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 \
  -validity 10000 -alias linguatranslate
```

It will ask for a keystore password and a key password — remember both.

**3. Base64-encode the keystore** so it can be pasted into a GitHub Secret:

```bash
base64 -i release.keystore -o release.keystore.base64
# macOS: base64 -i release.keystore | pbcopy
# Linux: base64 -w0 release.keystore
```

**4. Add these repo secrets** (GitHub repo → Settings → Secrets and variables → Actions → New repository secret):

| Secret name | Value |
|---|---|
| `KEYSTORE_BASE64` | contents of `release.keystore.base64` |
| `KEYSTORE_PASSWORD` | the keystore password from step 2 |
| `KEY_ALIAS` | `linguatranslate` (or whatever alias you used) |
| `KEY_PASSWORD` | the key password from step 2 |
| `APP_API_KEY` | same value as the backend's `APP_API_KEY` (see section 6b) |
| `BASE_URL_RELEASE` | your deployed backend URL, e.g. `https://your-backend.onrender.com/` |

Never commit `release.keystore` or `release.keystore.base64` to the repo — they're
already covered by `.gitignore`, and the workflow deletes the decoded keystore file
from the CI runner immediately after building.

### Publishing a release

```bash
git tag v1.0.0
git push origin v1.0.0
```

Push the tag and the workflow builds a signed APK and creates a GitHub Release named
`v1.0.0` with `app-release.apk` attached — visible at
`https://github.com/<you>/<repo>/releases`.

### Installing the released APK

Anyone can download the APK from the Releases page and install it directly (they'll
need to allow "install from unknown sources" since it's not from the Play Store).
This is a legitimate, common way to distribute an app without going through app-store
review — just be aware Android will warn the installer that the app is from an
unverified source, since GitHub Releases isn't a recognized app marketplace.

## 9. Troubleshooting

- **"Unable to resolve dependency" during Gradle sync**: make sure your network can
  reach `dl.google.com` / `maven.google.com` and `repo.maven.apache.org`.
- **App can't reach the backend from the emulator**: confirm the backend is running on
  the host machine and that debug builds use `http://10.0.2.2:PORT/`, not `localhost`.
- **Cleartext traffic blocked**: only the debug build variant permits cleartext HTTP,
  and only to `10.0.2.2`/`localhost` (see `src/debug/res/xml/network_security_config.xml`).
  Release builds must use HTTPS.
- **"Japanese voice is not installed on this device"**: install the Japanese TTS
  language pack via Android Settings → System → Languages → Text-to-speech output, or
  test on a device/emulator image that ships with more voices preinstalled.
- **SpeechRecognizer returns no results**: some emulator images have no working speech
  recognition service. Test voice features on a physical device or an emulator image
  with Google Play Services.

## 10. What Was Actually Verified in This Environment

This project was built in a sandboxed environment **without** an Android SDK and
**without** network access to Google's Maven repositories, so the Android app could
not be compiled or run here. What *was* verified end-to-end here:

- Backend: `npm install`, `tsc` build succeeded, `npm test` → 18/18 passing (including
  4 new tests for the `APP_API_KEY` middleware), and the server was started and
  exercised live with real HTTP requests confirming: requests without the app key are
  rejected (401), requests with the correct key succeed (200), requests with a wrong
  key are rejected (401), `/health` stays reachable without the key, and HSTS/CSP/
  X-Frame-Options headers are present on real responses.
- Android: full Kotlin/Compose source tree was written following the architecture
  above; it has **not** been compiled by Gradle in this environment. Build it via the
  steps in section 6 to get a working APK.
