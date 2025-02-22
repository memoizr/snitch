repositories {
    mavenCentral()
}

dependencies {
    api(libs.undertow.core)
    api(project(":core"))
    api(project(":types"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        jvmTarget = "17"
    }
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "undertow"
        }
    }
}