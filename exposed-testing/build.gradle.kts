dependencies {
    api(project(":exposed"))

    implementation(platform(libs.junit.bom))
    implementation(libs.junit.jupiter)
    implementation(libs.junit.jupiter.api)
    implementation(libs.mockk)
    implementation(libs.assertj.core)
}