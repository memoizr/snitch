repositories {
    mavenCentral()
}


dependencies {
    implementation(kotlin("stdlib"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "snitch"
            artifactId = "types"
            version = "1.0"

            from(components["java"])
        }
    }
}
