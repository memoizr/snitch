plugins {
    kotlin("jvm") version "1.8.20"
    `maven-publish`
    `java-library`
}

group = "com.snitch.spark"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    api("com.sparkjava:spark-core:2.9.4")
    api(project(":core"))
    api("ch.qos.logback:logback-classic:1.2.9")
    implementation(kotlin("stdlib"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "me.snitchon"
            artifactId = "sparkjava"
            version = "1.0"

            from(components["java"])
        }
    }
}