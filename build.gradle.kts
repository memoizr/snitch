
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

// Load version from properties file
val versionPropsFile = file("version.properties")
val versionProps = Properties()
versionPropsFile.inputStream().use { versionProps.load(it) }
val projectVersion = versionProps.getProperty("version")

plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
    `java-library`
    id("com.vanniktech.maven.publish") version "0.30.0"
}

allprojects {
    group = "io.github.memoizr"
    version = projectVersion

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

        if (file("gradle-local.properties").exists()) {
            apply(from= "gradle-local.properties")
        }
        mavenPublishing {
            coordinates("io.github.memoizr", "snitch-${project.name}", projectVersion)

            pom {
                name.set("Snitch")
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

tasks.register("updateVersionInDocs") {
    group = "documentation"
    description = "Updates version references in documentation files"
    
    doLast {
        val version = project.version.toString()
        val regex = """implementation\("io\.github\.[\.]*memoizr:snitch-.*?:(\d+\.\d+\.\d+)"\)""".toRegex()
        val testRegex = """testImplementation\("io\.github\.[\.]*memoizr:snitch-.*?:(\d+\.\d+\.\d+)"\)""".toRegex()
        var totalUpdated = 0
        
        // Find all markdown files in the guides directory
        fileTree("guides").matching {
            include("**/*.md")
        }.forEach { file ->
            val content = file.readText()
            
            // Replace version in implementation strings
            val updatedContent = content.replace(regex) { matchResult ->
                val fullString = matchResult.value
                val groupIndex = fullString.indexOf("snitch-")
                val colonIndex = fullString.lastIndexOf(":")
                val artifact = fullString.substring(groupIndex, fullString.indexOf(":", groupIndex))
                val prefix = fullString.substring(0, groupIndex)
                "$prefix$artifact:$version\")"
            }.replace(testRegex) { matchResult ->
                val fullString = matchResult.value
                val groupIndex = fullString.indexOf("snitch-")
                val colonIndex = fullString.lastIndexOf(":")
                val artifact = fullString.substring(groupIndex, fullString.indexOf(":", groupIndex))
                val prefix = fullString.substring(0, groupIndex)
                "$prefix$artifact:$version\")"
            }
            
            // Only update the file if changes were made
            if (content != updatedContent) {
                file.writeText(updatedContent)
                println("Updated version references in: ${file.relativeTo(projectDir)}")
                totalUpdated++
            }
        }
        
        println("Finished updating version references in $totalUpdated files.")
    }
}

tasks.register("llmdocs") {
    group = "documentation"
    description = "Concatenates all .md files from the guides/docs directory into a single SnitchLLMDocs.md file"
    dependsOn("updateVersionInDocs")

    doLast {
        val outputFile = file("SnitchLLMDocs.md")
        outputFile.delete() // Remove existing file if it exists
        outputFile.createNewFile()

        // Define the logical order of files and directories
        val orderedFiles = mutableListOf<File>()
        
        // First add top-level docs that should come first
        val whatIsSnitch = file("guides/docs/What is Snitch.md")
        if (whatIsSnitch.exists()) {
            orderedFiles.add(whatIsSnitch)
        }
        
        // Then add the main docs file
        val mainDocs = file("guides/docs/Docs.md")
        if (mainDocs.exists()) {
            orderedFiles.add(mainDocs)
        }
        
        // Add files from tutorials directory
        val tutorialsDir = file("guides/docs/tutorials")
        if (tutorialsDir.exists() && tutorialsDir.isDirectory) {
            tutorialsDir.walk()
                .filter { it.isFile && it.extension.equals("md", ignoreCase = true) }
                .sortedBy { it.name }
                .forEach { orderedFiles.add(it) }
        }
        
        // Add files from in-depth directory
        val inDepthDir = file("guides/docs/in depth")
        if (inDepthDir.exists() && inDepthDir.isDirectory) {
            inDepthDir.walk()
                .filter { it.isFile && it.extension.equals("md", ignoreCase = true) }
                .sortedBy { it.name }
                .forEach { orderedFiles.add(it) }
        }
        
        // Add files from resources directory
        val resourcesDir = file("guides/docs/resources")
        if (resourcesDir.exists() && resourcesDir.isDirectory) {
            resourcesDir.walk()
                .filter { it.isFile && it.extension.equals("md", ignoreCase = true) }
                .sortedBy { it.name }
                .forEach { orderedFiles.add(it) }
        }
        
        // Add any remaining .md files from the docs directory that weren't explicitly included
        file("guides/docs").walk()
            .maxDepth(1)
            .filter { 
                it.isFile && 
                it.extension.equals("md", ignoreCase = true) && 
                it != whatIsSnitch && 
                it != mainDocs 
            }
            .sortedBy { it.name }
            .forEach { orderedFiles.add(it) }
        
        // Write the concatenated content to the output file
        outputFile.bufferedWriter().use { writer ->
            writer.write("# Snitch Documentation\n\n")
            writer.write("*This file was automatically generated by concatenating documentation files.*\n\n")
            writer.write("---\n\n")
            
            orderedFiles.forEach { file ->
                println("Adding: ${file.relativeTo(projectDir)}")
                writer.write("## From: ${file.name}\n\n")
                writer.write(file.readText())
                writer.write("\n\n---\n\n")
            }
        }
        
        println("Documentation compiled to: ${outputFile.absolutePath}")
        println("Total files processed: ${orderedFiles.size}")
    }
}
