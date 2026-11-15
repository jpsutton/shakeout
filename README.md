# Shakeout

A single-player dice game for Android. Roll five dice up to three times a turn, hold the ones you want, and fill
in the scorecard. Pick a mode before you start:

- **Regular**: one scorecard, 13 turns.
- **Triple**: three scorecards worth x1, x2 and x3, 39 turns.

The scoring rules, and which Triple details are design choices rather than checked against a rulebook, are in
[docs/RULES.md](docs/RULES.md). Shakeout is an independent game, not affiliated with or endorsed by any publisher.

An unfinished game is saved and can be resumed; the best score of each mode is kept. No ads, accounts or network
access.

## Install

Shakeout runs on Android 8.0 or newer. The easiest way to install and update it is Shakeout's F-Droid repository,
which works in F-Droid, Droid-ify and Neo Store:

1. On the phone, open [jpsutton.github.io/shakeout/fdroid](https://jpsutton.github.io/shakeout/fdroid/) and tap
   **Add to F-Droid**.
2. Install **Shakeout** from that repository. New releases then show up as regular app updates.

To add the repository by hand, use the address `https://jpsutton.github.io/shakeout/fdroid/repo` and check that its
fingerprint is:

```
ED8C395DCEA3488FCC340BC00F4DF494C35FDFD8EDCAC94F4A63C41D23FAF44E
```

You can also download `shakeout-<version>.apk` from the
[Releases page](https://github.com/jpsutton/shakeout/releases/latest) and install it directly. Android asks you to
allow installs from your browser the first time.

## Build

Needs JDK 21 and Android SDK platform 37, or Docker:

```bash
./gradlew test lint assembleRelease
scripts/docker-gradle.sh test lint assembleRelease   # same, in a container; no SDK needed
```

## Layout

- `app/src/main/java/io/github/jpsutton/shakeout/game/`: the rules, with no Android dependencies (`Scoring`,
  `Game`, `GameState`, `GameCodec`). JVM unit tests are in `app/src/test`.
- `.../ui/`: Jetpack Compose screens and the `GameViewModel`; `.../data/Store.kt`: saved game and best scores.
- `packaging/fdroid/`: F-Droid metadata and icon. `scripts/ci/`: release scripts.

## Branches and releases

Changes go feature branch -> pull request into `staging` (static checks only, no build) -> pull request
`staging` -> `release`. The `release` pull request runs the tests, lint and release build (`build.yml`). Merge it with
"Create a merge commit" after bumping `VERSION_NAME` in `gradle.properties`; `release.yml` then signs that build's
APK without rebuilding, tags `v<version>`, creates a GitHub release and updates the F-Droid repository on the
`gh-pages` branch.

### Release keys

`release.yml` needs these repository secrets (base64 PKCS12 keystores and their passwords):

- `SHAKEOUT_APK_KEYSTORE`, `SHAKEOUT_APK_KEYSTORE_PASS`: signs the APK. Android refuses an update signed by a
  different key, so this key must never change or be lost.
- `SHAKEOUT_FDROID_KEYSTORE`, `SHAKEOUT_FDROID_KEYSTORE_PASS`: signs the F-Droid index; its fingerprint is what
  clients pin. Alias `shakeout-fdroid`.

## License

MIT, see [LICENSE](LICENSE).
