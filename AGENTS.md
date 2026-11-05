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

## License & distribution (decided 2026-11-05)

- **`GPL-3.0-or-later`** (`LICENSE` file at repo root). F-Droid metadata license field will be `GPL-3.0-or-later`.
- Implications the user accepted: modified forks may be sold for profit (must stay GPL + carry source); unaltered copies may also be sold — no FOSS license can prevent that. Enforcement lever for the *brand*: the "Mostly" name/logo stay trademark-custodied by the user, so forks must rebrand. Commercial dual-licensing inquiries go to the user as sole copyright holder.
- Copyright header convention for new source files: short SPDX notice (`SPDX-License-Identifier: GPL-3.0-or-later`) in the file top comment of new `.kt` files.

## Current next step (pointer for fresh sessions)

- Infrastructure (scaffold, CI, git workflow, license) is done and verified. The next feature is the **E3 data model** — stories US-3.1 to US-3.3 (terms → courses → slots), to be built as `feature/us-3-1-to-3-3-terms-courses-slots` off `dev`, unit tests first per the Feature process below. UI stories (E1/E2) follow after the data model exists.

## Git workflow (decided 2026-11-05)

- **Branch model:** `feature/<us-x-y-slug>` branches off **`dev`**; merge back with `git merge --no-ff`. `fix/<slug>` for bugfix branches, same target.
- **`main` = published state.** Only touched by release merges and tags — never commit features directly to `main`.
- **Release flow:** merge `dev` → `main` with `--no-ff`, bump `versionCode` (monotonic integer) and `versionName` (semver-ish `X.Y.Z`) in `app/build.gradle.kts`, create annotated tag `vX.Y.Z` on `main`. F-Droid builds reference these tags, so tag the exact commit you publish.
- **CI:** `.github/workflows/ci.yml` builds the debug APK and runs `testDebugUnitTest` on every push/PR touching `dev`/`main`. Repo live at `github.com/Lemerald/Mostly` (pushed 2026-11-05); merge-gating comes from GitHub branch protection ("required status checks", check name `build-and-test`) — a setting, not code. A local pre-push hook (`core.hooksPath` → `.githooks/`) runs the unit tests before every push; bypass with `--no-verify`.

## Feature process (decided 2026-11-05)

For each new feature, in order:
1. Add/adjust user stories in `docs/user-stories.md` (with acceptance criteria) before branching.
2. Branch `feature/<us-…>` off `dev`; extract acceptance criteria into an implementation checklist.
3. Plan the code structure; write unit tests **before implementing any logic** (data model, slot/term generation, date bucketing, RRULE math). UI composition is exempt — verify UI via instrumented tests or on-device checks instead.
4. Implement until tests + acceptance criteria pass; merge to `dev` with `--no-ff`.

## Conventions for future work

- When scaffolding new features, follow the Must-first story order in the spec and keep acceptance criteria testable; add unit/integration tests per story as implemented and list the suites here.
