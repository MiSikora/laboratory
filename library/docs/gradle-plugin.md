# Gradle plugin

Gradle plugin's main job is to make your life easier when creating and managing feature flags. It generates features, feature factories, and customized sourced feature storage. Plugin, additionally, verifies things that cannot be represented by the API. For example, it checks if a feature flag has exactly one default value defined.

Under the hood Gradle plugin uses [KotlinPoet](https://square.github.io/kotlinpoet/) to generate compact source files.

!!! info
    Gradle plugin automatically adds the `laboratory` artifact to dependencies.

!!! tip
    The best way to understand the Gradle plugin is to check [the sample](https://github.com/MiSikora/Laboratory/tree/master/sample). It uses most of the Gradle plugin features that most of the applications need.

## Feature flags

Feature flags are added to the generation process with a `feature()` function which uses the `generateFeatureFlags` Gradle task. Here is a sample configuration.

```groovy
apply plugin: "io.mehow.laboratory"

laboratory {
  packageName = "io.mehow.laboratory.sample"

  feature("Authentication") {
    description = "Type of authentication when opening the app"

    withValue("None")
    withValue("Fingerprint")
    withDefaultValue("Retina")
  }

  feature("LocationTracking") {
    packageName = "io.mehow.laboratory.location"

    isPublic = false

    withValue("Enabled")
    withDefaultValue("Disabled")

    withDefaultSource("Firebase")
    withSource("Aws")
  }
}
```

This setup creates two feature flags. `Authentication` and `LocationTracking` with values taken from the `feature(name) { }` block. Key things that might not be that obvious.

- Feature flag source visibility is inherited from a feature's visibility.
- If a feature flag defines a remote source a `Local` source is automatically added as an enum value. Any custom `Local` sources will be filtered out.
- If all sources are added with `withSource()` function, `Local` source will be used as a default one.

```kotlin
package io.mehow.laboratory.sample

import io.mehow.laboratory.Feature
import kotlin.Boolean
import kotlin.String

public enum class Authentication(
  public override val isDefaultValue: Boolean = false
) : Feature<Authentication> {
  Password,
  Fingerprint,
  Retina(isDefaultValue = true),
  ;

  public override val description: String = "Type of authentication when opening the app"
}
```

```kotlin
package io.mehow.laboratory.location

import io.mehow.laboratory.Feature
import java.lang.Class
import kotlin.Boolean
import kotlin.Suppress

internal enum class LocationTracking(
  public override val isDefaultValue: Boolean = false
) : LocationTracking<Authentication> {
  Enabled,
  Disabled(isDefaultValue = true),
  ;

  @Suppress("UNCHECKED_CAST")
  public override val sourcedWith: Class<Feature<*>> = Source::class.java as Class<Feature<*>>

  internal enum class Source(
    public override val isDefaultValue: Boolean = false
  ) : Feature<Source> {
    Local,
    Firebase(isDefaultValue = true),
    Aws,
    ;
  }
}
```

## Feature flags storage

If your feature flags use multiple sources you can configure Gradle plugin to generate for you a quality of life extension function that returns a custom `FeatureStorage`.

```groovy
apply plugin: "io.mehow.laboratory"

laboratory {
  packageName = "io.mehow.laboratory.sample"

  sourcedStorage()

  feature("FeatureA") {
    withValue("Enabled")
    withDefaultValue("Disabled")

    withSource("Azure")
    withSource("Firebase")
  }

  feature("FeatureB") {
    withValue("Enabled")
    withDefaultValue("Disabled")

    withSource("Azure")
    withSource("Aws")
  }

  feature("FeatureC") {
    withValue("Enabled")
    withDefaultValue("Disabled")

    withSource("Heroku")
  }

  feature("FeatureD") {
    withDefaultValue("Enabled")
    withValue("Disabled")
  }
}
```

`sourcedStorage()` function uses `generateSourcedFeatureStorage` Gradle task that generates the code below.

```kotlin
package io.mehow.laboratory.sample

import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.FeatureStorage.Companion.sourced
import kotlin.collections.mapOf
import kotlin.to

internal fun FeatureStorage.Companion.sourcedGenerated(
  localSource: FeatureStorage,
  azureSource: FeatureStorage,
  firebaseSource: FeatureStorage,
  awsSource: FeatureStorage,
  herokuSource: FeatureStorage
): FeatureStorage = sourced(
  localSource,
  mapOf(
    "Azure" to azureSource,
    "Firebase" to firebaseSource,
    "Aws" to awsSource,
    "Heroku" to herokuSource,
  )
)
```

!!! tip
    Use named arguments when you instantiate `FeatureStorage` with the `sourcedGenerated()` function. This will make sure that you accidentally don't mess up arguments order and that the compilation fails whenever there are changes to the sources.

## Feature flags factory

Generation of feature flags factory is useful if you use the [QA module](qa-module.md).

```
apply plugin: "io.mehow.laboratory"

laboratory {
  packageName = "io.mehow.laboratory.sample"

  featureFactory()

  feature("FeatureA") {
    withValue("Enabled")
    withDefaultValue("Disabled")
  }

  feature("FeatureB") {
    withValue("Enabled")
    withDefaultValue("Disabled")
  }

  feature("FeatureC") {
    withValue("Enabled")
    withDefaultValue("Disabled")
  }
}
```

`featureFactory()` uses `generateFeatureFactory` Gradle task that generates the code below. `Class.forname()` is used for lookup instead of the direct reference to classes because there is no guarantee that feature flags are directly available in the module that generates the factory if feature flags come, for example, as transitive dependencies of other modules.

```kotlin
package io.mehow.laboratory.sample

import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import java.lang.Class
import kotlin.Suppress
import kotlin.collections.Set
import kotlin.collections.setOf

internal fun FeatureFactory.Companion.featureGenerated(): FeatureFactory = GeneratedFeatureFactory

private object GeneratedFeatureFactory : FeatureFactory {
  @Suppress("UNCHECKED_CAST")
  public override fun create() = setOf(
    Class.forName("io.mehow.laboratory.sample.FeatureA"),
    Class.forName("io.mehow.laboratory.sample.FeatureB"),
    Class.forName("io.mehow.laboratory.sample.FeatureC")
  ) as Set<Class<Feature<*>>>
}
```

## Feature flag sources factory

If you want to group all feature flag sources in a similar way to feature flags you can use `featureSourceFactory()` function that collects them.

```groovy
laboratory {
  packageName = "io.mehow.laboratory.sample"

  featureSourceFactory()

  feature("FeatureA") {
    withValue("Enabled")
    withDefaultValue("Disabled")

    withSource(Remote)
  }

  feature("FeatureB") {
    withValue("Enabled")
    withDefaultValue("Disabled")

    withSource(Remote)
  }
}
```

This uses the `generateFeatureSourceFactory` Gradle task that generates the code below.

```kotlin
package io.mehow.laboratory.sample

import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import java.lang.Class
import kotlin.Suppress
import kotlin.collections.Set
import kotlin.collections.setOf

internal fun FeatureFactory.Companion.featureSourceGenerated(): FeatureFactory =
    GeneratedFeatureSourceFactory

private object GeneratedFeatureSourceFactory : FeatureFactory {
  @Suppress("UNCHECKED_CAST")
  public override fun create() = setOf(
    Class.forName("io.mehow.laboratory.sample.FeatureA${'$'}Source"),
    Class.forName("io.mehow.laboratory.sample.FeatureB${'$'}Source")
  ) as Set<Class<Feature<*>>>
}
```

## Multi-module support

Gradle plugin was written with a support for multi-module projects in mind.

```
.
├─ module-a
│  └─ build.gradle
├─ module-b
│  └─ build.gradle
├─ module-app
│  └─ build.gradle
├─ build.gradle
└─ settings.gradle
```

A Laboratory setup for a Gradle project like above could look like this. Configuration of Android Gradle plugin or any other dependencies are omitted for brevity.

```groovy
// module-a
plugins {
  id "org.jetbrains.kotlin.jvm"
  id "io.mehow.laboratory"
}

laboratory {
  packageName = "com.sample.a"

  feature("Authentication") {
    withDefaultValue("Password")
    withValue("Fingerprint")
    withValue("Retina")
    withValue("Face")

    withSource("Firebase")
    withSource("Aws")
  }

  feature("AllowScreenshots") {
    withValue("Enabled")
    withDefaultValue("Disabled")
  }
}
```

```groovy
// module-b
plugins {
  id "org.jetbrains.kotlin.jvm"
  id "io.mehow.laboratory"
}

laboratory {
  packageName = "com.sample.b"

  feature("DistanceAlgorithm") {
    isPublic = false

    withDefaultValue("Euclidean")
    withValue("Jaccard")
    withValue("Cosine")
    withValue("Edit")
    withValue("Hamming")

    withSource("Firebase")
    withDefaultSource("Azure")
  }
}

dependencies {
  implementation project(":module-a")
}
```

```groovy
// module-app
plugins {
  id "com.android.application"
  id "org.jetbrains.kotlin.android"
  id "io.mehow.laboratory"
}

laboratory {
  packageName = "com.sample"
  sourcedStorage()
  featureFactory()
}

dependencies {
  implementation project(":module-b")
}
```

This setup shows that each module can define its own feature flags that do not have to be exposed outside. In this scenario `module-app` is responsible only for gluing together all feature flags in a way that `Laboratory` instances are aware of feature flag sources and the [QA module](qa-module.md). It should then deliver the correct `Laboratory` to modules via dependency injection.

Gradle plugin discovers all feature flags and theirs sources that are a part of the same project. There might be some rare cases when you'd like to exclude some modules from contributing its feature flags to the `featureFactory()` or `sourcedStorage()`. This can be achieved with project filtering.

```groovy
apply plugin: "io.mehow.laboratory"

laboratory {
  featureFactory {
    excludeProjects { project -> project.name == "module-a" }
  }

  sourcedStorage {
    excludeProjects { project -> project.name == "module-a" }
  }
}
```

This way `:module-a` will not contribute its feature flags to generation of a feature factory and feature storage.

## Full configuration

Below is the full configuration of the Gradle plugins.

```groovy
laboratory {
  // Sets namespace of generated features and factories. Empty by default.
  packageName = "io.mehow.sample"

  // Informs plugin to create 'enum class SomeFeature' during the generation period.
  feature("SomeFeature") {
    // Overrides globally declared namespace. No value by default.
    packageName = "io.mehow.sample.feature"

    // Adds description to this feature that can be used for more context.
    description = "Feature description"

    // Sets visibility of a feature flag to be either 'public' or 'internal'. 'true' by default.
    isPublic = false

    // Informs plugin to add 'ValueA' value to the generated feature flag and set it as a default value.
    // Exactly one of the feature values must be set with this function.
    withDefaultValue("ValueA")

    // Informs plugin to add 'ValueB' value to the generated feature flag.
    withValue("ValueB")

    // Informs plugin to add 'Firebase' value to the list of sources controlling this flag.
    // Adding any source automatically adds 'Local' value to the source enum.
    // Any custom 'Local' sources are ignored by the plugin.
    withSource("Firebase")

    // Informs plugin to add 'Aws' value to the list of sources controlling this flag and to set is a default value.
    // At most one of the source values can be set with this function.
    // By default 'Local' sources are considered default values.
    withDefaultSource("Aws")
  }

  // Configures feature flags storage. Useful when feature flags have multiple sources.
  sourcedStorage {
    // Overrides globally declared namespace. No value by default.
    packageName = "io.mehow.sample.storage"

    // Sets visibility of a storage extension function to be either 'public' or 'internal'. 'false' by default.
    isPublic = true

    // Excludes projects from contributing sources to this storage. Includes all projects by default.
    excludeProjects { project -> true }
  }

  // Configures feature flags factory useful for the QA module configuration.
  featureFactory {
    // Overrides globally declared namespace. No value by default.
    packageName = "io.mehow.sample.factory"

    // Sets visibility of a factory extension function to be either 'public' or 'internal'. 'false' by default.
    isPublic = true

    // Excludes projects from contributing features to this factory. Includes all projects by default.
    excludeProjects { project -> true }
  }

  // Configures feature flag sources factory.
  featureSourceFactory {
    // Overrides globally declared namespace. No value by default.
    packageName = "io.mehow.sample.factory"

    // Sets visibility of a factory extension function to be either 'public' or 'internal'. 'false' by default.
    isPublic = true

    // Excludes projects from contributing features to this factory. Includes all projects by default.
    excludeProjects { project -> true }
  }
}
```
