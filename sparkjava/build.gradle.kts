plugins {
    kotlin("jvm")
    maven
}

group = "com.snitch.spark"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    api("com.sparkjava:spark-core:2.9.3")
    api(project(":core"))
    api("ch.qos.logback:logback-classic:1.1.7")
    implementation(kotlin("stdlib"))
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
