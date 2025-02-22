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
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.21")

    implementation(project(":core"))
    implementation(project(":gsonparser"))

    api("com.github.memoizr:assertk-core:-SNAPSHOT")
    api("org.assertj:assertj-core:3.18.1")
    api("ch.qos.logback:logback-classic:1.1.7")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "tests"
        }
    }
}
