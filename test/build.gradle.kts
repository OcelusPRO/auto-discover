plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":api"))
    ksp(project(":processor"))

    // Ajout de la dépendance de test native de Kotlin
    testImplementation(kotlin("test"))
}

// Optionnel, mais recommandé : pour que Gradle utilise le runner de test de Kotlin
tasks.withType<Test> {
    useJUnitPlatform() // kotlin.test s'intègre avec JUnit Platform Runner
}