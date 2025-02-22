dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":types"))
    
    implementation(libs.hibernate.validator)
    implementation(libs.jakarta.el)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
}
