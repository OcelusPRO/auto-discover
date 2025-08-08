plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

allprojects {
    group = "fr.ftnl.tools"
    version = "1.0.1"

    repositories {
        mavenCentral()
    }


    plugins.withType<PublishingPlugin> {
        extensions.configure<PublishingExtension> {
            repositories {
                mavenLocal {
                    url = rootProject.layout.buildDirectory.dir("repo").get().asFile.toURI()
                }
            }
        }
    }
}

subprojects {
    plugins.withType<PublishingPlugin> {

        val tachePublicationTemporaire = tasks.register<PublishToMavenRepository>("publicationVersRepoTemporaire") {
            group = "publishing" // C'est une bonne pratique de grouper les tâches internes
            description = "Tâche interne pour publier dans un dépôt temporaire pour le ZIP."
            publication = project.extensions.getByType<PublishingExtension>().publications.getByName("mavenJava") as MavenPublication
            repository = project.repositories.maven {
                name = "repoTemporairePourZip"
                // On place le dépôt temporaire dans le dossier build du module
                url = project.layout.buildDirectory.dir("repo-publication").get().asFile.toURI()
            }
        }

        tasks.register<Zip>("creerZipPublication") {
            group = "publishing"
            description = "Crée une archive ZIP de la publication de ce module pour un envoi manuel."

            // La tâche de ZIP dépend maintenant de la tâche de publication déclarée juste au-dessus.
            dependsOn(tachePublicationTemporaire)

            // La source du ZIP est le dossier rempli par la tâche de publication.
            from(tachePublicationTemporaire.get().repository.url)

            destinationDirectory.set(project.layout.buildDirectory.dir("distributions"))
            archiveFileName.set("${project.name}-${project.version}.zip")

            doLast {
                logger.lifecycle("✅ Le ZIP de publication pour '${project.name}' a été créé ici : ${archiveFile.get().asFile.absolutePath}")
            }
        }
    }
}


tasks.register("publishSnapshot") {
    group = "publishing"
    description = "Publie tous les modules en version SNAPSHOT."

    // On déclare que cette tâche dépend de toutes les vraies tâches de publication
    // dans les sous-projets. `withType` est plus robuste que de chercher par nom.
    dependsOn(subprojects.flatMap { it.tasks.withType<PublishToMavenRepository>() })
}
gradle.taskGraph.whenReady {
    // Si la tâche "publishSnapshot" fait partie de l'exécution demandée...
    if (hasTask(":publishSnapshot")) {
        // ...et si la version n'est pas déjà un snapshot...
        if (!project.version.toString().endsWith("-SNAPSHOT")) {
            // ...alors on modifie la version pour tous les projets.
            allprojects {
                version = "${project.version}-SNAPSHOT"
            }
            logger.lifecycle("✅ Version changée en '${project.version}' pour la publication snapshot.")
        }
    }
}