dependencies {
    implementation(libs.kotlin.reflect)

    implementation(project(":core"))
    implementation(project(":gsonparser"))

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
