
plugins {
    kotlin("jvm") version "2.0.20"
    application
    idea
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
    implementation("org.apache.xmlgraphics:batik-dom:1.19")
    implementation("org.apache.xmlgraphics:batik-swing:1.19")
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