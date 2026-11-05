# AGENTS.md

## Status

- New Android project, freshly scaffolded. Single Gradle module `:app`.
- Product spec lives in [`docs/user-stories.md`](docs/user-stories.md) — read it before planning or implementing features. It contains fixed decisions (marked), story priorities (Must = MVP), and the resolved former open questions.

## Product scope (from docs/user-stories.md)

- Native Android **student calendar**: calendar + to-dos (incl. course tasks) + courses/timetable ("terms only" model — per-term schedule plus free-day list; no A/B weeks).
- MVP = stories marked Must. The app has exactly three top-level views: Calendar (default), To-dos, Timetable — matches the bottom nav already wired in `MainActivity`.

## Architecture constraints (decided — do not violate)

- **Stack:** Kotlin + Jetpack Compose, single `:app` module. Versions pinned in `gradle/libs.versions.toml`.
- **Event storage:** Android **CalendarProvider** (`CalendarContract`) is the only source of truth for calendar events. No own event database, no duplicate event store.
- **Tasks storage:** Room is the task source of truth (offline-first); `VTODO` in `.ics` exports (RFC 5545); DAVx5 sync via Tasks.org provider bridge is backlog story US-5.3 — do not require it.
- **CalDAV sync:** delegated entirely to **DAVx5**. The app must not implement its own CalDAV client or sync logic.
- Generated timetable events must be written to the app's own managed calendar so they can be identified and regenerated.
- `READ_CALENDAR`/`WRITE_CALENDAR` permissions are already in the manifest; request at runtime **after** the user performs an action that needs them.

## Verified developer commands (run from repo root)

Always export the JDK first — see **JDK quirk** below.

```sh
export JAVA_HOME=/path/to/full-jdk-17-21   # see quirk below
./gradlew assembleDebug        # builds app-debug.apk to app/build/outputs/apk/debug/
./gradlew testDebugUnitTest    # unit tests (none exist yet)
```

- Android SDK lives at `/home/leonarddf/Android/Sdk` (platforms android-35, build-tools 35.0.1), wired via `local.properties` — **not committed**, recreate per machine with `sdk.dir=...`.
- Namespace / applicationId: `io.github.lemerald.mostly` (GitHub username space, confirmed by user 2026-11-05; display-label mismatch with "Planner" heritage was renamed before first publish).

## JDK quirk (high signal)

- The system default `java` is **JDK 27**, which Gradle 8.10.2 refuses to run on. `/usr/lib/jvm/java-17-openjdk` is **JRE-only (no javac)** — also unusable.
- Use a full JDK 17–21. A working one was previously fetched to `/tmp/opencode/jdk-21.0.13+9` (ephemeral — recreate if gone):
  ```sh
  curl -fsSL -o /tmp/opencode/jdk21.tar.gz "https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse" \
    && tar xzf /tmp/opencode/jdk21.tar.gz -C /tmp/opencode
  ```
  Longer-term, installing `jdk21-openjdk` via pacman would remove this step.

## Toolchain quirks

- `material-icons-extended` was added because core Compose icons are missing many symbols (`Event`, `Checklist`, `ViewWeek`); it inflates the debug APK (~16 MB) — R8-in release builds prunes unused icons.
- Gradle wrapper distribution 8.10.2 + AGP 8.7.3 + Kotlin 2.0.21 + compileSdk/targetSdk 35, minSdk 26 (chosen for notification channels + Compose support; documented as decision #2 in the spec).

## Conventions for future work

- When scaffolding new features, follow the Must-first story order in the spec and keep acceptance criteria testable; add unit/integration tests per story as implemented and list the suites here.
