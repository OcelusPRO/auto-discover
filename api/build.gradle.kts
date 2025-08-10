/*
 * auto-discover - auto-discover.api
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
    `java-library`
}

extensions.configure<PublishingExtension> {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            name.set("API for Auto-Discover")
            description.set("The API annotations for the Auto-Discover library.")
        }
    }
}