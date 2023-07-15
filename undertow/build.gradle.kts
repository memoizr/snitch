plugins {
    kotlin("jvm") version "1.9.0"
    `maven-publish`
    `java-library`
}

group = "com.snitch.spark"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    api("io.undertow:undertow-core:2.3.6.Final")
    api(project(":core"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        jvmTarget = "17"
    }
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "snitch"
            artifactId = "undertow"
            version = "1.0"

            from(components["java"])
        }
    }
}