plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
    `java-library`
    signing
}



// Définissez vos informations de projet
group = "fr.ftnl.tools"
version = "1.0.0"


java {
    // Il est crucial d'inclure les sources et la javadoc
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            // Informations du projet qui apparaîtront sur Maven Central
            pom {
                name.set("autodiscover-processor")
                description.set("Annotation pour la découverte de services via KSP.")
                url.set("https://github.com/OcelusPRO/auto-discover")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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
    repositories {
        maven {
            name = "CentralPortal"
            url = uri("https://central.sonatype.com/api/v1/publisher")
            credentials {
                username = System.getenv("OSSRH_USERNAME") ?: findProperty("ossrhUsername")?.toString()
                password = System.getenv("OSSRH_PASSWORD") ?: findProperty("ossrhPassword")?.toString()
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        findProperty("signing.keyId") as String,
        findProperty("signing.gnupg.key") as String,
        findProperty("signing.password") as String
    )
    // Signe notre publication
    sign(publishing.publications["mavenJava"])
}
