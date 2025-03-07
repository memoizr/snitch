dependencies {
    implementation(project(":core"))
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.coroutines.core)
    implementation(libs.kotlin.reflect)

    testImplementation(project(":tests"))
    testImplementation(project(":undertow"))
    testImplementation(project(":gsonparser"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}
