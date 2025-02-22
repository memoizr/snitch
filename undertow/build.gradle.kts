dependencies {
    api(project(":core"))
    api(project(":types"))
    api(libs.undertow.core)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "undertow"
        }
    }
}