dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":types"))
    implementation(project(":core"))
    implementation(project(":shank"))

    implementation(libs.argon2)
    implementation(libs.jwt.api)
    runtimeOnly(libs.jwt.impl)
    runtimeOnly(libs.jwt.gson)

    testImplementation(project(":tests"))
    testImplementation(project(":undertow"))
    testImplementation(project(":gsonparser"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk)
    testImplementation(libs.assertk.core)
    testImplementation(kotlin("test"))
}