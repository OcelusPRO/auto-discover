# AutoDiscover

[![Maven Central](https://img.shields.io/maven-central/v/fr.ftnl.tools/auto-discover-bom.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:fr.ftnl.tools)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**AutoDiscover** is a lightweight Kotlin library that uses [KSP (Kotlin Symbol Processing)](https://kotlinlang.org/docs/ksp-overview.html) to automate service discovery and registration, inspired by Java's standard `ServiceLoader` mechanism.

No more manual creation and maintenance of files in `META-INF/services/`! Just annotate your classes and let the processor do the work.

## Features

- **Zero Configuration**: No need to manually create service files anymore.
- **Type Safety**: Everything is checked at compile time.
- **Simple and Lightweight**: No complex dependencies, just a clean API and an annotation processor.
- **Error Detection**: The API helps you detect build configuration errors when your application starts.

---

## Setup

To use AutoDiscover, you need to set up the KSP plugin and add the library's dependencies to your project.

**Step 1: Apply the KSP Plugin**

Make sure the KSP plugin is applied in your `build.gradle.kts` file.

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" // Use the KSP version that matches your Kotlin version
}
```

**Step 2: Add Dependencies**

We strongly recommend using our **Bill of Materials (BOM)** to manage the versions of the `api` and `processor` modules and ensure their compatibility.

Add the following lines to the `dependencies` block of your `build.gradle.kts` file:

```kotlin
dependencies {
    // 1. Import the BOM to manage versions
    implementation(platform("fr.ftnl.tools:auto-discover-bom:1.0.1"))

    // 2. Add the library dependencies
    // Versions are managed by the BOM, you don't need to specify them.
    implementation("fr.ftnl.tools:auto-discover-api")
    ksp("fr.ftnl.tools:auto-discover-processor")
}
```

---

## Usage

Usage is designed to be as simple as possible.

### Step 1: Define a Service Interface

Create an interface that your services will implement.

```kotlin
interface MyService {
    fun doSomething()
}
```

### Step 2: Create an Implementation and Annotate It

Create a class that implements your interface and add the `@AutoDiscover` annotation.

```kotlin
import fr.ftnl.tools.autoDiscover.api.AutoDiscover

@AutoDiscover
class MyServiceImpl : MyService {
    override fun doSomething() {
        println("The service was discovered and is working!")
    }
}
```

### Step 3: Retrieve the Service

Use the `AutoDiscoverer` object to retrieve all implementations of your service.

```kotlin
import fr.ftnl.tools.autoDiscover.api.AutoDiscoverer

fun main() {
    // Retrieves a list of all implementations of MyService
    val services = AutoDiscoverer.get<MyService>()

    if (services.isNotEmpty()) {
        val myService = services.first()
        myService.doSomething() // Prints "The service was discovered and is working!"
    } else {
        println("No service found.")
    }
}
```

**Important**: If you forget to add the `ksp("fr.ftnl.tools:processor")` dependency, the call to `AutoDiscoverer.get<MyService>()` will throw an `IllegalStateException` with a clear error message explaining how to fix your build.

---

## License

This project is distributed under the Apache 2.0 License. See the `LICENSE` file for more details.
