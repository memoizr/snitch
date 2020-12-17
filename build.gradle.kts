import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    maven
}

group = "me.user"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
    maven("https://dl.bintray.com/arrow-kt/arrow-kt/")
    maven("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
}
dependencies {
    implementation("io.ktor:ktor-client-cio:1.4.1")
    implementation("io.ktor:ktor-client-gson:1.4.1")
    implementation("io.ktor:ktor-server-netty:1.4.1")

    implementation("com.jsoniter:jsoniter:0.9.19")

    implementation("com.beust:klaxon:5.4")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("ch.qos.logback:logback-classic:1.1.7")

    testImplementation(kotlin("test-junit"))
    testImplementation("khttp:khttp:1.0.0")
    testImplementation(project(":sparkjava"))
    testImplementation(project(":jooby"))

    testImplementation("com.github.memoizr:assertk-core:-SNAPSHOT")
}
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}