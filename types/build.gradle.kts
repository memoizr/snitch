repositories {
    mavenCentral()
}


dependencies {
    implementation(kotlin("stdlib"))
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "types"
        }
    }
}
