plugins {
    kotlin("jvm") version "1.5.10"
    // kotlinx-serialization: Serialization library
    kotlin("plugin.serialization") version "1.5.10"
    application
}

group = "ru.spbu.math-cs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    // kotlinx-serialization: Serialization library
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    // Clikt: Command-line interface library
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}