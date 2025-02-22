import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

tasks.test {
    useJUnitPlatform()
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "17"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "17"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "coroutines"
        }
    }
}
