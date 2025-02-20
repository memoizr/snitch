import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
}

repositories {
    mavenCentral()
}
group = "me.user"
version = "1.0-SNAPSHOT"

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "17"
}
