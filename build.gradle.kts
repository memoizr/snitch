import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
    `java-library`
    id("com.vanniktech.maven.publish") version "0.30.0"
}

allprojects {
    group = "io.github.memoizr"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("java-library")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        }
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }

    if (project.name != "example") {
        apply(plugin = "com.vanniktech.maven.publish")
        mavenPublishing {
            coordinates("io.github.memoizr", "snitch-${project.name}", "1.0.0")

            pom {
                name.set("Snitchg")
                description.set("A web framework for Kotlin.")
                inceptionYear.set("2020")
                url.set("https://memoizr.github.io/snitch")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("memoizr")
                        name.set("memoizr")
                        url.set("https://github.com/memoizr/")
                    }
                }
                scm {
                    url.set("https://github.com/memoizr/snitch/")
                    connection.set("scm:git:git://github.com/memoizr/snitch.git")
                    developerConnection.set("scm:git:ssh://git@github.com/memoizr/snitch.git")
                }
            }
            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
            signAllPublications()
        }

    }
}
