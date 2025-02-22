repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    api(libs.gson)
    implementation(libs.kotlin.reflect)
    implementation(project(":core"))
    api(project(":types"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "gsonjsonparser"
        }
    }
}
