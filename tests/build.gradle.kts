dependencies {
    implementation(project(":core"))
    implementation(project(":gsonparser"))
    implementation(project(":shank"))
    implementation(libs.kotlin.reflect)

    api(platform(libs.junit.bom))
    api(libs.junit.jupiter)
    api(libs.mockk)
    api(libs.assertk.core)
    api(libs.assertj.core)
    api(libs.logback.classic)
}
