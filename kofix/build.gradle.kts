repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.dom4j:dom4j:2.1.4")
    implementation("org.apache.commons:commons-lang3:3.15.0")
    implementation("org.javassist:javassist:3.30.2-GA")
    implementation("io.github.classgraph:classgraph:4.8.171")
    implementation(libs.kotlin.reflect)

    testImplementation("com.github.memoizr:assertk-core:-SNAPSHOT")
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(kotlin("test-junit"))
}

tasks.withType<JavaCompile> {
    options.forkOptions.jvmArgs = listOf(
        "--add-opens=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED",
        "--add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED"
    )
}
