plugins {
    kotlin("jvm")
}

group = "com.snitch.jooby"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib"))
    implementation("io.jooby:jooby-utow:2.9.4")
    implementation("io.jooby:jooby:2.9.4")
}
