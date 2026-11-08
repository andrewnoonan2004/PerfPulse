# PerfPulse ⚡

**PerfPulse** is a modern, Material 3 Expressive Android performance monitor with live CPU, GPU, RAM, thermal, and process diagnostics. It adapts automatically to your wallpaper's dynamic color palette and scales across phone, foldable, and tablet form factors.

---

## ✨ Features

- **Material 3 Expressive Design**:
  - Built with `MaterialExpressiveTheme` and `MotionScheme.expressive()`.
  - Dynamic wallpaper-based coloring powered by Material You (Android 12+).
  - Material 3 Expressive shapes (`Cookie12Sided`, `Sunny`, `Clover4Leaf`, `Pill`) and expressive wave indicators (`CircularWavyProgressIndicator` and `LinearWavyProgressIndicator`).

- **Adaptive Layout**:
  - Automatically switches between a **Bottom Navigation Bar** on compact devices and a **Navigation Rail** on large screens, foldables, and tablets via `NavigationSuiteScaffold`.
  - Responsive multi-column layout using `LazyVerticalGrid` (1, 2, or 3 columns based on width breakpoints).

- **CPU Telemetry**:
  - Real-time CPU utilization gauge with automatic fallback from `/proc/stat` jiffies to per-core clock-scaling telemetry (ensuring non-root functionality on modern Android).
  - Hardware SoC identification, ABI architectures, and CPU clusters (Efficiency, Performance, Prime).
  - Per-core real-time frequency gauges (MHz/GHz), scaling range, governor, and online/sleeping states.
  - Live 60-second rolling sparkline trend chart.

- **GPU Telemetry**:
  - Vendor kernel node probing across Qualcomm Adreno (`/sys/class/kgsl`), ARM Mali (`/sys/devices/platform/*.mali`, `devfreq`), and MediaTek GED.
  - Dynamic clock frequency gauges with fallback when direct OEM paths are sandboxed.
  - Live OpenGL ES renderer, vendor, version string, and Vulkan hardware API feature level.
  - Live rolling GPU load sparkline chart.

- **RAM & Swap / ZRAM**:
  - Live used vs. available RAM with low-memory warning alert.
  - Detailed memory allocations: Page Cache (Cached), Active, Inactive reclaimable memory, and Buffers/Shmem.
  - ZRAM & Swap usage, capacity, and utilization percentage.
  - Live memory utilization trend chart.

- **Process & Application Vitals**:
  - **Own Process Diagnostics**: PID, UID, process name, Total PSS footprint, Java/ART heap, Native heap, active thread count, CPU time, and uptime.
  - **System App Usage**: Real foreground runtime in the last 24 hours via Android `UsageStatsManager`, complete with filter/search and a one-click button to open System Settings if permission is needed.

- **Interactive Controls**:
  - Live sampling interval chips (500 ms, 1 s, 2 s).
  - Live Pause / Resume monitoring button.
  - Battery temperature and thermal headroom / throttling forecast.

---

## 🛠️ Architecture & Tech Stack

- **UI**: Jetpack Compose, Material 3 Expressive (1.5.0-beta02), Compose Adaptive Navigation Suite
- **Architecture**: MVVM with unidirectional data flow and Kotlin Coroutines / `StateFlow`
- **Testing**: 25 unit and integration tests (JUnit 4, Kotlinx Coroutines Test, Robolectric 4.17 + Compose UI Testing)
- **CI/CD**: GitHub Actions workflow building and publishing non-debug (release) APKs on push and tag

---

## 📦 Building

### Local Build

To run unit tests:
```bash
./gradlew testDebugUnitTest
```

To build a non-debug (release) APK:
```bash
./gradlew assembleRelease
```
The output APK will be generated at `app/build/outputs/apk/release/PerfPulse-release.apk`.

### GitHub Actions CI/CD

This repository includes a continuous integration workflow (`.github/workflows/build-release.yml`) that:
1. Runs all unit and integration tests.
2. Builds the non-debug (release) APK.
3. Uploads `PerfPulse-release.apk` as a build artifact on every push to `main` and pull request.
4. Automatically attaches the APK to a GitHub Release whenever a version tag (e.g. `v1.0.0`) is pushed.

---

## 📄 License

Apache License 2.0
