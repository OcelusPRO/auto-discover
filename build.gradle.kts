import io.github.gradlenexus.publishplugin.NexusPublishExtension
import org.jetbrains.dokka.gradle.DokkaTask
import org.gradle.api.plugins.JavaPluginExtension


plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.dokka) apply false
}

allprojects {
    group = "fr.ftnl.tools"
    version = "1.0.2"

    repositories {
        mavenCentral()
    }
}

extensions.configure<NexusPublishExtension> {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("OSSRH_USERNAME") ?: findProperty("ossrhUsername")?.toString())
            password.set(System.getenv("OSSRH_PASSWORD") ?: findProperty("ossrhPassword")?.toString())
        }
    }
}

subprojects {
    if (project.name == "test") return@subprojects

    plugins.apply("org.gradle.maven-publish")
    plugins.apply("signing")
    plugins.apply("org.jetbrains.dokka")

    plugins.withId("javaLibrary") {
        // Tâche pour créer le JAR de la Javadoc (KDoc)
        val javadocJar by tasks.registering(Jar::class) {
            dependsOn(tasks.named<DokkaTask>("dokkaHtml"))
            archiveClassifier.set("javadoc")
            from(tasks.named<DokkaTask>("dokkaHtml").get().outputDirectory)
        }

        extensions.configure<PublishingExtension> {
            publications.named<MavenPublication>("mavenJava") {
                artifact(javadocJar)
            }
        }
    }


    extensions.configure<SigningExtension> {
        useGpgCmd()
        sign(extensions.getByType<PublishingExtension>().publications)
    }

    extensions.configure<PublishingExtension> {
        publications {
            register<MavenPublication>("mavenJava") {
                artifactId = "${rootProject.name}-${project.name}"

                plugins.withId("java-library") {
                    from(project.components["java"])
                    val sourcesJar by project.tasks.registering(Jar::class) {
                        archiveClassifier.set("sources")
                        val sourceSets = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
                        from(sourceSets["main"].allSource)
                    }
                    val javadocJar by project.tasks.registering(Jar::class) {
                        archiveClassifier.set("javadoc")
                        dependsOn(project.tasks.named("dokkaHtml", DokkaTask::class))
                        from(project.tasks.named("dokkaHtml", DokkaTask::class).get().outputDirectory)
                    }
                    artifact(sourcesJar)
                    artifact(javadocJar)
                }

                plugins.withId("java-platform") {
                    from(components["javaPlatform"])
                }

                pom {
                    url.set("https://github.com/OcelusPRO/${rootProject.name}")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("oceluspro")
                            name.set("ocelus_ftnl")
                            email.set("contact@ftnl.fr")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/oceluspro/${rootProject.name}.git")
                        developerConnection.set("scm:git:ssh://github.com/oceluspro/${rootProject.name}.git")
                        url.set("https://github.com/oceluspro/${rootProject.name}")
                    }
                }
            }
        }
    }
}