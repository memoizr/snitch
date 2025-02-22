repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlin.stdlib.jdk8)

    implementation(libs.coroutines.core)
    implementation(libs.kotlin.reflect)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(project(":tests"))
    testImplementation(project(":undertow"))
    testImplementation(project(":gsonparser"))
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "coroutines"
        }
    }
}
