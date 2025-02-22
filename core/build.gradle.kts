dependencies {
    implementation(project(":types"))
    implementation(libs.kotlin.reflect)
    implementation(libs.coroutines.core)
    implementation(libs.snakeyaml)

    testImplementation(project(":undertow"))
    testImplementation(project(":gsonparser"))
    testImplementation(project(":tests"))
    testImplementation(libs.assertk.core)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "core"
        }
    }
}