/*
 * auto-discover - auto-discover.bom
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