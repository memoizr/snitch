dependencies {
    api(libs.undertow.core)
    api(project(":core"))
    api(project(":types"))
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