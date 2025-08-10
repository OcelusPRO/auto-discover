/*
 * auto-discover - auto-discover.processor
 * Copyright (C) 2025 ocelus_ftnl
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    `java-library`
}

dependencies {
    implementation(project(":api"))
    implementation(libs.ksp.api)
}

extensions.configure<PublishingExtension> {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            name.set("Processor for Auto-Discover")
            description.set("The KSP processor for the Auto-Discover library, enabling automatic discovery of components.")
        }
    }
}