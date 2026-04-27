plugins {
    kotlin("jvm") version "2.0.20"
    application
    idea
    id("org.beryx.runtime") version "2.0.0"
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

group = "com.stochastictinkr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.stochastictinkr:skywing:0.1-SNAPSHOT")
    implementation("com.github.weisj:jsvg:2.0.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.stochastictinkr.cards.solitaire.MainKt")
}

kotlin {
    jvmToolchain(21)
}

runtime {
    options = listOf(
        "--strip-debug",
        "--compress", "2",
        "--no-header-files",
        "--no-man-pages"
    )
    modules = listOf(
        "java.desktop",
        "java.prefs",
        "java.xml",
        "java.logging",
    )

    launcher {
        noConsole = true
    }

    jpackage {
        appVersion = "1.0.0"
    }
}