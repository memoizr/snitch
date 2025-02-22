repositories {
    mavenCentral()
}

dependencies {
    api("io.undertow:undertow-core:2.3.6.Final")
    api(project(":core"))
    api(project(":types"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        jvmTarget = "17"
    }
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "snitch"
            artifactId = "undertow"
            version = "1.0"

            from(components["java"])
        }
    }
}