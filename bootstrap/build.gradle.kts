repositories {
    mavenCentral()
}

dependencies {
    api(project(":core"))
    api(project(":gsonparser"))
    api(project(":undertow"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "bootstrap"
        }
    }
}
