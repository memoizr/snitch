import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
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
