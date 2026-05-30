# Particle Flow

> This is a fork of [nfaralli/particleflow](https://github.com/nfaralli/particleflow), modernized with AI to be fully compatible with Android 16 (API 36).

A live wallpaper and demo app showing up to 50,000 particles animated via OpenGL ES and native C++ (JNI).

- Touch and drag attraction points to influence particle movement
- Particle color indicates speed (blue = slow, red = fast)
- Settings are editable via the gear icon (long-press to open)

## Build

Requires **JDK 21**, **Android SDK API 36**, **NDK 29**, and **CMake 3.31**.

```bash
./gradlew assembleDebug
```

APKs are generated in `app/build/outputs/apk/`.

## GitHub Releases

Tagged releases are built automatically via GitHub Actions.
Create a tag to trigger a release:

```bash
git tag v1.3.0
git push origin v1.3.0
```

## Tech Stack

- Kotlin
- OpenGL ES 2.0
- NDK / JNI (particle physics in C++)
- Android Gradle Plugin 9.x
