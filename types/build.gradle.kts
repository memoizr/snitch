plugins {
    kotlin("jvm") version "1.9.21"
    `maven-publish`
    `java-library`
}

group = "me.snitchon"
version = "1.0-SNAPSHOT"
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
