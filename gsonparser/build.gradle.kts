dependencies {
    api(libs.gson)
    implementation(libs.kotlin.reflect)
    implementation(project(":core"))
    api(project(":types"))
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "gsonjsonparser"
        }
    }
}
