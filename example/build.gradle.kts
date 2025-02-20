plugins {
    kotlin("jvm") version "1.9.21"
}

group = "me.user"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":undertow"))
    implementation(project(":gsonparser"))

    implementation("org.postgresql:postgresql:42.7.2")
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.41.1")
    implementation("de.mkammerer:argon2-jvm:2.10")
    implementation("io.mockk:mockk:1.12.0")

    implementation("com.github.memoizr:shank:3.0.0")
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    implementation("ch.qos.logback:logback-classic:1.4.12")
    implementation("net.logstash.logback:logstash-logback-encoder:6.6")


    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.2")
    runtimeOnly("io.jsonwebtoken:jjwt-gson:0.11.2")

    testImplementation("com.github.memoizr:momster:fac1dae13d")


    testImplementation(project(":tests"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
