plugins {
    kotlin("jvm")
    maven
}

group = "com.snitch.undertow"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
    implementation(kotlin("stdlib"))
    implementation("io.undertow:undertow-core:2.2.3.Final")
}
