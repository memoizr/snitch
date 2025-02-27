dependencies {
    // Main dependencies
    implementation(project(":types"))
    implementation(libs.kotlin.reflect)
    implementation(libs.coroutines.core)
    implementation(libs.snakeyaml)

    // Test dependencies
    testImplementation(project(":tests"))
    testImplementation(project(":undertow"))
    testImplementation(project(":gsonparser"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.mockk)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertk.core)
}
