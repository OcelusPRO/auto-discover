plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    `java-library`
    `maven-publish`
    signing
}

dependencies {
    implementation(project(":api"))
    implementation(libs.ksp.api)
}


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
    val signingKeyId = findProperty("signing.keyId")
    val signingKey = findProperty("signing.gnupg.key")
    val signingPassword = findProperty("signing.password")

    if (signingKeyId != null && signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(
            signingKeyId as String,
            signingKey as String,
            signingPassword as String
        )
        sign(publishing.publications["mavenJava"])
    }
}
