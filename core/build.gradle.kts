plugins {
    jacoco
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
    maven("https://dl.bintray.com/arrow-kt/arrow-kt/")
    maven("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
}

dependencies {
    implementation(libs.kotlin.reflect)
    implementation(libs.coroutines.core)
    implementation(libs.snakeyaml)
    implementation(project(":types"))

    testImplementation(libs.kotlin.test.junit)
    testImplementation(project(":undertow"))
    testImplementation(project(":gsonparser"))
    testImplementation(libs.assertk.core)
    testImplementation(project(":tests"))
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "core"
        }
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}
jacoco {
    toolVersion = libs.versions.jacoco.get()
    reportsDirectory.set(layout.buildDirectory.dir("customJacocoReportDir"))
}
tasks.jacocoTestReport {
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}