plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    `java-library`
}

dependencies {
    implementation(project(":api"))
    implementation(libs.ksp.api)
}

extensions.configure<PublishingExtension> {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            name.set("Processor for Auto-Discover")
            description.set("The KSP processor for the Auto-Discover library, enabling automatic discovery of components.")
        }
    }
}