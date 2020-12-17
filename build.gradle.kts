import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
}

repositories {
    mavenCentral()
}
group = "me.user"
version = "1.0-SNAPSHOT"

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
