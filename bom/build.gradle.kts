plugins {
    `java-platform` // Le plugin magique pour les BOMs
}

// Pas besoin de group/version ici si c'est déjà dans allprojects

dependencies {
    // On déclare que notre plateforme (BOM) définit les versions
    // pour les modules 'api' et 'processor'.
    constraints {
        api(project(":api"))
        api(project(":processor"))
    }
}

extensions.configure<PublishingExtension> {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            name.set("BOM for Auto-Discover")
            description.set("A Bill of Materials (BOM) for the Auto-Discover library, defining versions for its modules.")
        }
    }
}