dependencies {
    implementation(project(":core"))
    implementation(project(":gsonparser"))
    implementation(project(":shank"))
    implementation(libs.kotlin.reflect)
    implementation("com.jayway.jsonpath:json-path:2.7.0")

    implementation(platform(libs.junit.bom))
    implementation(libs.junit.jupiter)
    implementation(libs.junit.jupiter.api)
    implementation(libs.mockk)
    implementation(libs.assertj.core)

    testImplementation(libs.assertk.core)


}
