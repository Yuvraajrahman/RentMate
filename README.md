# RentMate — Expense Tracker for Roommates

A beautiful expense tracking app with iOS-style glassmorphism UI built with Jetpack Compose and Room database.

## Features Implemented

✅ **Feature 1:** User accounts (Admin + Roommates with local authentication)  
✅ **Feature 2:** Flat profile settings (name, address, billing due day, bkash number)  
✅ **Feature 3:** Add/view expenses with categories (Rent, Utilities, Meals, Extras)  
✅ **Feature 4:** Per-user expense split calculation  
✅ **Feature 6:** Monthly summary screen with per-user breakdown  
✅ **Login/Logout:** Support for multiple users on same device  
✅ **Navigation:** Bottom nav with Home, Expenses, Summary, and Profile screens  
✅ **UI:** Liquid glass effect inspired by iOS with smooth animations

## How to Run on Windows 10 with Android Studio

1. **Install Android Studio** (latest stable). During setup, install SDK, platform tools, and one emulator image (API 30+).
2. **Open project:** File → Open → select this `RentMate` folder.
3. **Sync Gradle:** Wait for sync. Accept SDK licenses if prompted.
4. **Create emulator:** Tools → Device Manager → Create Device → Pixel 5 → API 30+ → Start.
5. **Run:** Select emulator in toolbar and click Run ▶.

## Test Credentials

The app comes pre-seeded with 4 users:

| Username  | Password     | Role     |
|-----------|--------------|----------|
| Admin     | Admin        | Admin    |
| Emon      | emon123      | Roommate |
| Bokhtiar  | bokhtiar123  | Roommate |
| Mahir     | mahir123     | Roommate |

## How to Test

1. **Login** with any of the above credentials
2. **Add expenses** from the Home screen or Expenses tab
3. **Select participants** to split costs among specific roommates
4. **View summary** to see per-person breakdown
5. **Logout** from Profile tab and login as different user
6. **Admin only:** Access Settings from Home screen to configure flat details

## Architecture

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material3
- **Database:** Room (SQLite) for local storage
- **Navigation:** Jetpack Navigation Compose
- **State:** StateFlow for reactive UI updates

## Notes

- All data stored locally in Room database
- All users share same device (login/logout flow)
- Cloud sync and automated emails will be added in future updates
- Currency: BDT (৳)