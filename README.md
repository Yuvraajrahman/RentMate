# RentMate — Local Demo (Feature 1 & 2 Complete)

What you can test now:
- Login with Admin/Admin (Feature 1 — local auth via Room, seeded admin)
- Flat Profile settings (name, address, due day=10 default, bkash number) (Feature 2)
- Jetpack Compose UI with subtle glassmorphism

## Run on Windows 10 with Android Studio

1. Install Android Studio (latest stable). During setup, install SDK, platform tools, and one emulator image.
2. Open this folder (`RentMate`) in Android Studio (File → Open → select `RentMate`).
3. Let Gradle sync. If prompted, accept SDK licenses or updates.
4. Create an emulator: Tools → Device Manager → Create Device → Pixel 5 (or any) → select a System Image (API 30+), then Start.
5. In the toolbar, choose the running emulator and click Run ▶.
6. The app will launch to Login.

Credentials to test:
- Username: `Admin`
- Password: `Admin`

After login you land on Flat Settings. Edit and press Save. Values are stored locally in Room.

## Notes
- This project currently uses local storage only (Room DB). Cloud/sync can be added later.
- Monthly billing window day is editable here; email/bkash features will be added next.

## Tech
- Kotlin, Jetpack Compose, Room, Material3.