import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
}

group = "com.stochastictinkr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    sourceArtifacts(kotlin("jvm"))
    implementation("com.stochastictinkr:skywing:1.0-SNAPSHOT")
    sourceArtifacts("com.stochastictinkr:skywing:1.0-SNAPSHOT")
    implementation("org.apache.xmlgraphics:batik-dom:1.15")
    implementation("org.apache.xmlgraphics:batik-swing:1.15")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}