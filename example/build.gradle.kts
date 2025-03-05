dependencies {
    // Project dependencies
    implementation(project(":core"))
    implementation(project(":undertow"))
    implementation(project(":gsonparser"))
    implementation(project(":shank"))

    // Database
    implementation(libs.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)

    // Security & Utils
    implementation(libs.argon2)
    implementation(libs.mockk)
//    implementation(libs.shank)

    // JWT
    implementation(libs.jwt.api)
    runtimeOnly(libs.jwt.impl)
    runtimeOnly(libs.jwt.gson)

    // Logging
    implementation(libs.logback.classic)
    implementation(libs.logstash.encoder)

    // Testing
    testImplementation(project(":tests"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.assertj.core)
    testImplementation(libs.assertk.core)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.momster)
}
