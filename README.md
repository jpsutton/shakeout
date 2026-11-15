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

Add the F-Droid repository to F-Droid, Droid-ify or Neo Store:
`https://jpsutton.github.io/shakeout/fdroid/repo` (the fingerprint is on
`https://jpsutton.github.io/shakeout/fdroid/`), or take the APK from the
[releases](https://github.com/jpsutton/shakeout/releases).

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
