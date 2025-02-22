import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.kotlin.reflect)

    implementation(project(":core"))
    implementation(project(":gsonparser"))

    api(libs.assertk.core)
    api(libs.assertj.core)
    api(libs.logback.classic)
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "tests"
        }
    }
}
