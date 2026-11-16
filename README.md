# River Flood Watch - Android Application

River Flood Watch is a production-grade Android application designed for monitoring river discharge and flood risk forecasts powered by Copernicus GloFAS data via the Open-Meteo Flood API.

---

## Features

- 🔐 **Firebase Authentication & Google Sign-In:** Authenticate using real Google credentials, view profile information, or continue in Guest mode.
- 🌊 **30-Day Flood Risk Forecasts:** Fetches real river discharge data ($m^3/s$) from Open-Meteo Flood API.
- 📊 **Peak Detection & Daily Discharge:** Highlights 30-day peak discharge and today's current discharge.
- 🚥 **Automated Risk Level Classification:**
  - **Normal:** Discharge $\le 1.5 \times \text{mean}$ (Green)
  - **Watch:** $1.5 \times \text{mean} < \text{Discharge} \le 2.0 \times \text{mean}$ (Yellow/Orange)
  - **Alert:** Discharge $> 2.0 \times \text{mean}$ (Red)
- 📍 **Location Awareness & Place Management:** Detects location via `FusedLocationProviderClient` or add custom monitored locations with coordinate validation ($[-90, 90]$ lat, $[-180, 180]$ lon).
- 💾 **Room Persistence:** Persists monitored places locally using Room Database.
- 🔔 **Background Alerts & Notifications:** Daily `WorkManager` background job checks 7-day forecasts and posts high-priority local notifications if an `ALERT` level is detected.
- ♿ **Accessibility & UI Polish:** Content descriptions for risk badges, responsive Material3 components, and accessible color contrast.

---

## Build & Setup Instructions

### Prerequisites
- Android Studio 2026.1+ or JDK 17+
- Android SDK 35/36+
- Gradle Wrapper (included)

### Firebase Google Sign-In Configuration
1. Open the [Firebase Console](https://console.firebase.google.com/) and create a project.
2. Add an Android app with package name `com.example.androiddemo`.
3. Obtain your SHA-1 fingerprint by running:
   ```bash
   ./gradlew signingReport
   ```
4. Add the SHA-1 fingerprint in Firebase Console under **Project Settings > Your apps > SHA certificate fingerprints**.
5. Enable **Google Sign-In** under **Firebase Console > Authentication > Sign-in method**.
6. Download `google-services.json` and place it in the `app/` directory (`app/google-services.json`).

### Building and Running the App
1. Assemble Debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
2. Run Unit Tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```
3. Run Android Lint:
   ```bash
   ./gradlew lintDebug
   ```

---

## Data & API Reference

- **API Endpoint:** `https://flood-api.openmeteo.com/v1/flood`
- **Data Source:** Copernicus GloFAS global flood forecast model (~5km resolution).
- **Disclaimer:** Model forecasts provide estimated river discharge metrics. Always consult local official emergency authorities for binding flood warnings.
