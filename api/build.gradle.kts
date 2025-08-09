plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

extensions.configure<PublishingExtension> {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            name.set("API for Auto-Discover")
            description.set("The API annotations for the Auto-Discover library.")
        }
    }
}