Project Guidelines for Advanced Contributors

This document captures project-specific knowledge to accelerate development, debugging, and testing of the Cards/Solitaire application.

Overview
- Language/Toolchain: Kotlin/JVM, Kotlin 2.0.20, JVM toolchain 21.
- Build System: Gradle (Kotlin DSL) with wrapper.
- Entry Point: com.stochastictinkr.cards.solitaire.MainKt.
- Test Framework: kotlin.test (running on JUnit Platform).
- Rendering/Assets: Apache Batik for SVG, resources under src/main/resources/cards.
- Domain: Playing cards primitives (Card, Suit, Rank) and a Klondike-style Solitaire implementation (tableau, foundations, waste, etc.).

Testing
- Framework: kotlin.test with JUnit Platform (configured via tasks.test.useJUnitPlatform()).
- Source layout: place tests under src/test/kotlin mirroring main package structure when convenient.
- Useful targets:
  - ./gradlew test — run all tests
  - ./gradlew test --tests "com.stochastictinkr.cards.*" — filter by packages/classes
  - ./gradlew cleanTest test — re-run all tests from scratch

Adding a New Test
- Prefer deterministic tests over UI/animation behavior (the animation/render layers depend on Swing/Batik and are not ideal for unit tests).
- Focus tests on pure logic: standard deck generation, rank sequencing, tableau move validation, foundation rules.
- Example unit test that was verified locally (created temporarily to validate instructions):

  File: src/test/kotlin/com/stochastictinkr/cards/StandardDeckTest.kt
  package com.stochastictinkr.cards

  import com.stochastictinkr.cards.standard.StandardDeck
  import kotlin.test.Test
  import kotlin.test.assertEquals

  class StandardDeckTest {
      @Test
      fun `standard deck has 52 unique cards`() {
          val cards = StandardDeck.cards
          assertEquals(52, cards.size, "Deck should have 52 cards")
          assertEquals(52, cards.toSet().size, "Deck should have 52 unique cards")
      }
  }

- To run just this test:
  ./gradlew test --tests "com.stochastictinkr.cards.StandardDeckTest"

- Notes:
  - StandardDeck is a data object (not a class). Access cards via StandardDeck.cards.
  - Card exposes derived properties: color and name; component3() returns color, enabling destructuring like val (_, rank, color) = card.

Project-Specific Development Notes
- Solitaire logic
  - Tableau receiving rules implemented in com.stochastictinkr.cards.solitaire.TableauReceiver:
    - Empty tableau column only accepts a KING as the top card of a moved sequence when both visible and hidden stacks are empty.
    - Otherwise, sequences must alternate color and strictly descend in rank (e.g., red 7 on black 8). The extension isValidTableau validates an in-order sequence (top to bottom) before a move.
  - Foundation rules and other move sources/receivers are in the solitaire package. Use these classes for unit testing game logic without rendering.
- Rendering and assets
  - SVG assets live under src/main/resources/cards. Rendering uses Apache Batik (dom + swing) via SvgPainter utilities.
  - Keep heavy or UI-bound logic out of unit tests; rely on logic-only components for reliability.
- Dependencies
  - com.stochastictinkr:skywing:0.1-SNAPSHOT is used (assumed available locally or via mavenLocal). If absent, publish to mavenLocal or remove/replace features from it.
  - Apache Batik 1.19 is used for SVG handling.

Troubleshooting
- If build fails due to missing skywing artifact:
  - Ensure it exists in mavenLocal: ~/.m2/repository/com/stochastictinkr/skywing/0.1-SNAPSHOT
  - Or temporarily comment out the dependency in build.gradle.kts to work on core logic only. Keep the test scope working with kotlin.test.
- JDK mismatch:
  - The Gradle Kotlin DSL config requests jvmToolchain(21). Use a JDK 21 distribution, or configure Gradle toolchains in your environment.

Conventions
- Kotlin official code style (gradle.properties).
- Keep solitaire rules encoded in small, composable classes (e.g., CardSource/CardReceiver pairs). This makes unit testing straightforward.
- Prefer pure functions/immutable state where possible in logic layers to make tests reliable and deterministic.

Verification Performed for This Document
- Created a sample unit test verifying StandardDeck.cards contains 52 unique cards.
- Executed: ./gradlew test — the test passed under the current configuration.
- The temporary test file was removed after verification to keep the repository unchanged except for this guidelines file. See the example above if you need to recreate it.
