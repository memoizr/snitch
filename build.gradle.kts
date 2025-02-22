import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
    `maven-publish`
    `java-library`
}

allprojects {
    group = "me.user"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("maven-publish")
        plugin("java-library")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        }
    }

    // Add common publishing configuration
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "snitch"
                version = "1.0"
                // artifactId will be set by each subproject
                from(components["java"])
            }
        }
    }
}
