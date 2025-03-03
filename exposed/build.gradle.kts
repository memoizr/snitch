dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":types"))
    implementation(project(":core"))
    
    implementation(libs.hibernate.validator)
    implementation(libs.jakarta.el)
    implementation(libs.shank)

    api(libs.exposed.core)
    api(libs.exposed.jdbc)
    api(libs.exposed.java.time)

    testImplementation(project(":tests"))
    testImplementation(project(":undertow"))
    testImplementation(project(":gsonparser"))
    testImplementation(libs.h2database)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
}
