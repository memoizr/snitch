dependencies {
    implementation(project(":core"))
    implementation(project(":gsonparser"))
    implementation(libs.kotlin.reflect)

    api(libs.assertk.core)
    api(libs.assertj.core)
    api(libs.logback.classic)
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "tests"
        }
    }
}
