import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.21"

    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "cloud.drakon.tempestbot.defer"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("cloud.drakon:tempest:0.0.1-SNAPSHOT")

    implementation("com.amazonaws:aws-lambda-java-core:1.2.2")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    testImplementation(kotlin("test"))
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    named<ShadowJar>("shadowJar") {
        minimize()
    }
    test {
        useJUnitPlatform()
    }
}