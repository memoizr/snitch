plugins {
    kotlin("jvm") version "1.8.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation( "org.jetbrains.kotlin:kotlin-stdlib:1.8.20")
    implementation( "org.jetbrains.kotlin:kotlin-compiler-embeddable:1.8.20")
    implementation( "org.jetbrains.kotlin:kotlin-reflect:1.8.20")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}