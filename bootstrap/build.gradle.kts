dependencies {
    api(project(":core"))
    api(project(":gsonparser"))
    api(project(":undertow"))
    api(project(":validation"))
    api(project(":shank"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
