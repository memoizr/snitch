import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
    `maven-publish`
    `java-library`
}

group = "com.snitch.core"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
    maven("https://dl.bintray.com/arrow-kt/arrow-kt/")
    maven("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.21")
    implementation("org.yaml:snakeyaml:2.0")

    testImplementation(kotlin("test-junit"))
    testImplementation(project(":undertow"))
    testImplementation(project(":gsonparser"))
    testImplementation("com.github.memoizr:assertk-core:-SNAPSHOT")
    testImplementation(project(":tests"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "me.snitchon"
            artifactId = "core"
            version = "1.0"

            from(components["java"])
        }
    }
}
