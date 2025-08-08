plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

allprojects {
    // On définit le groupe et la version pour tout le monde
    group = "fr.ftnl.tools"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

// Définissez vos informations de projet
group = "fr.ftnl.tools"
version = "1.0.0"

