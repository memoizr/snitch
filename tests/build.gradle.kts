import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
    `maven-publish`
    `java-library`
}

group = "me.snitchon"
version = "1.0-SNAPSHOT"

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.21")

    implementation(project(":core"))
    implementation(project(":gsonparser"))

    api("com.github.memoizr:assertk-core:-SNAPSHOT")
    api("org.assertj:assertj-core:3.18.1")
    api("ch.qos.logback:logback-classic:1.1.7")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "snitch"
            artifactId = "tests"
            version = "1.0"

            from(components["java"])
        }
    }
}
