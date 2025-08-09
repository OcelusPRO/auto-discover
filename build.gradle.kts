import io.github.gradlenexus.publishplugin.NexusPublishExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.nexus.publish)
}

allprojects {
    group = "fr.ftnl.tools"
    version = "1.0.1-SNAPSHOT"

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

    extensions.configure<SigningExtension> {
        useGpgCmd()
        sign(extensions.getByType<PublishingExtension>().publications)
    }

    extensions.configure<PublishingExtension> {

        publications.create<MavenPublication>("mavenJava") {
            artifactId = "auto-discover-${project.name}"
            plugins.withId("java-library") {
                from(components["java"])
            }
            plugins.withId("java-platform") {
                from(components["javaPlatform"])
            }

            pom {
                url.set("https://github.com/OcelusPRO/auto-discover")
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
                    connection.set("scm:git:git://github.com/oceluspro/auto-discover.git")
                    developerConnection.set("scm:git:ssh://github.com/oceluspro/auto-discover.git")
                    url.set("https://github.com/oceluspro/auto-discover")
                }
            }
        }
    }
}