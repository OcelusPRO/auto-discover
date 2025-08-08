plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":api"))
    implementation(libs.ksp.api)
}
