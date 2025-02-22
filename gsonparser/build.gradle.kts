dependencies {
    api(project(":types"))
    api(libs.gson)

    implementation(project(":core"))
    implementation(libs.kotlin.reflect)
}
