import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

group = "cloud.drakon"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()

    maven {
        name = "KtDiscord"
        url = uri("https://maven.pkg.github.com/TempestProject/KtDiscord")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }

    maven {
        name = "KtUniversalis"
        url = uri("https://maven.pkg.github.com/drakon64/KtUniversalis")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }

    maven {
        name = "KtXivApi"
        url = uri("https://maven.pkg.github.com/drakon64/KtXivApi")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.amazonaws:aws-lambda-java-core:1.2.2")

    implementation("cloud.drakon:ktdiscord:6.1.0")

    val ktorVersion = "2.3.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-java:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("org.mongodb:mongodb-driver-sync:4.10.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // Translate
    implementation("aws.sdk.kotlin:translate:0.29.0-beta")

    testImplementation(kotlin("test"))
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    test {
        useJUnitPlatform()
    }
}
