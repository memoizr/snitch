repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":undertow"))
    implementation(project(":gsonparser"))

    implementation(libs.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.argon2)
    implementation(libs.mockk)

    implementation(libs.shank)
    implementation(libs.jwt.api)
    implementation(libs.logback.classic)
    implementation(libs.logstash.encoder)

    runtimeOnly(libs.jwt.impl)
    runtimeOnly(libs.jwt.gson)

    testImplementation(libs.momster)
    testImplementation(project(":tests"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}
