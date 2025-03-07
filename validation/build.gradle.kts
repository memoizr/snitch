dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":types"))
    implementation(project(":core"))
    implementation(project(":shank"))
    
    implementation(libs.hibernate.validator)
    implementation(libs.jakarta.el)

    testImplementation(project(":tests"))
    testImplementation(project(":undertow"))
    testImplementation(project(":gsonparser"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.assertk.core)
}
