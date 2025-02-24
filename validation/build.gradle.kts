dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":types"))
    implementation(project(":core"))
    
    implementation(libs.hibernate.validator)
    implementation(libs.jakarta.el)
    implementation(libs.shank)

    testImplementation(project(":tests"))
    testImplementation(project(":undertow"))
    testImplementation(project(":gsonparser"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
}
