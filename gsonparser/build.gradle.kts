dependencies {
    api(project(":types"))
    api(libs.gson)

    implementation(project(":core"))
    implementation(libs.kotlin.reflect)
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "gsonjsonparser"
        }
    }
}
