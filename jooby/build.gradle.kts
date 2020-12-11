plugins {
    kotlin("jvm")
}

group = "com.snitch.jooby"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
    implementation(kotlin("stdlib"))
    testImplementation("com.github.jooby-project:jooby:2.9.4")
}
