plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

// Définissez vos informations de projet
group = "fr.ftnl.tools"
version = "1.0.0"

