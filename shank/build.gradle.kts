dependencies {
    implementation(kotlin("stdlib"))

    testImplementation(libs.coroutines.core)
    testImplementation(libs.kotlin.reflect)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.mockk)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertk.core)
}
