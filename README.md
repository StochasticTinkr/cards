# Cards / Solitaire (Kotlin)

A Kotlin/JVM project implementing playing-card primitives and a Klondike-style Solitaire game with a simple Swing-based UI and SVG rendering via Apache Batik.

## Features
- Standard 52-card deck primitives (suits, ranks, colors, names)
- Klondike-style Solitaire logic (tableau, foundations, waste, stock)
- Move validation rules (alternating colors, descending ranks, KING on empty tableau column)
- Undo/Redo and New Game actions
- Multiple card back designs (selectable from the Deck menu)
- SVG assets for crisp rendering at any scale

## Tech stack
- Kotlin 2.0.20 targeting JVM
- JDK 21 (Gradle toolchain)
- Gradle (Kotlin DSL)
- Swing UI (via `java.desktop`)
- SVG rendering: Apache Batik 1.19 (`batik-dom`, `batik-swing`)
- Tests: `kotlin.test` on JUnit Platform
- Packaging: `org.beryx.runtime` plugin (runtime image / installers)

## Project layout
```
src/
  main/
    kotlin/
      com/stochastictinkr/cards/standard/        # Card, Suit, Rank, Color, StandardDeck
      com/stochastictinkr/cards/solitaire/       # Solitaire logic, UI component, app entry point
      com/stochastictinkr/svg/                   # SvgPainter utilities
      com/stochastictinkr/rendering/             # Animation helpers
    resources/
      cards/                                     # SVG card assets (fronts, backs, other)
  test/
    kotlin/
      com/stochastictinkr/cards/standard/        # Sample tests for deck/card primitives
```

Entry point: `com.stochastictinkr.cards.solitaire.MainKt`.

## Requirements
- JDK 21+
- Internet access for dependencies (or have them in local caches)
- On macOS/Linux/Windows, a JVM with GUI support (Swing/AWT)

Note: The build uses Gradle toolchains to fetch JDK 21 automatically if needed.

## Getting started
Clone the repo and run the game:

```bash
./gradlew run
```

Useful Gradle targets:
- Run all tests: `./gradlew test`
- Re-run tests from scratch: `./gradlew cleanTest test`
- Filter tests: `./gradlew test --tests "com.stochastictinkr.cards.*"`
- Create a minimized runtime image: `./gradlew runtime`
- Create native installers (via jpackage): `./gradlew jpackage` (platform support required)

The `runtime`/`jpackage` tasks come from the `org.beryx.runtime` Gradle plugin. This project configures:
- Stripped runtime with `java.desktop`, `java.prefs`, `java.xml`, `jdk.xml.dom`, `java.datatransfer`
- GUI launcher with console disabled

After `./gradlew runtime`, artifacts appear under `build/image/`. The launcher script is typically at `build/image/bin/cards`.

## Controls and menus
From the application menu bar:
- Game → New Game (⌘N on macOS, Ctrl+N on Windows/Linux)
- Game → Quit (⌘Q / Alt+F4)
- Deck → Choose among multiple card backs

Keyboard shortcuts within the window:
- Undo: ⌘Z (Ctrl+Z)
- Redo: ⇧⌘Z (Shift+Ctrl+Z)

The window starts maximized; on macOS, it uses an integrated/transparent title bar.

## Testing
This project uses `kotlin.test` on JUnit Platform.

Example (already included): `src/test/kotlin/com/stochastictinkr/cards/standard/StandardDeckTest.kt`.

Run tests:
```bash
./gradlew test
```

Prefer deterministic tests around pure logic (deck creation, rank sequences, tableau/foundation rules). Avoid UI or animation timing in unit tests.

## Troubleshooting
- Missing dependency `com.stochastictinkr:skywing:0.1-SNAPSHOT`:
  - Ensure it exists in your local Maven repo: `~/.m2/repository/com/stochastictinkr/skywing/0.1-SNAPSHOT`
  - Or temporarily comment out the dependency in `build.gradle.kts` to work on core logic and tests
- JDK mismatch: Ensure Gradle is using a JDK 21 toolchain. If you have multiple JDKs installed, set `JAVA_HOME` accordingly or rely on Gradle toolchains.

## Assets & rendering
- SVG assets are under `src/main/resources/cards` (fronts, backs, other variants)
- Rendering uses Apache Batik (DOM + Swing)
- Utilities live in `com.stochastictinkr.svg.SvgPainter`

## Code style & conventions
- Kotlin official code style (see `gradle.properties`)
- Solitaire rules are encoded in small, composable classes (sources/receivers)
- Favor pure functions/immutable state in logic to make tests reliable

## License
Specify your project license here. If bundling external artwork, ensure licenses permit redistribution.

## Acknowledgments
- Apache Batik for SVG rendering
- Kotlin and the broader JVM ecosystem
