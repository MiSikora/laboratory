# Gradle plugin

Gradle plugin's main job is to make your life easier when creating and managing feature flags. It generates features, feature factories, and customized sourced feature storage. Plugin, additionally, verifies things that cannot be represented by the API. For example, it checks if a feature flag has exactly one default option defined.

Under the hood, the Gradle plugin uses [KotlinPoet](https://square.github.io/kotlinpoet/) to generate compact source files.

!!! info
    The Gradle plugin automatically adds the `laboratory` artifact to dependencies.

!!! tip
    The best way to understand the Gradle plugin is to check [the samples](https://github.com/MiSikora/laboratory/tree/trunk/samples). It uses most of the Gradle plugin features that most of the applications need.

## Feature flags

Feature flags are added to the generation process with a `feature()` function, which uses the `generateFeatureFlags` Gradle task. Here is a sample configuration.

!!! tip
    Check [the sample](https://github.com/MiSikora/laboratory/tree/trunk/samples/basic) with demo configuration.

```groovy
apply plugin: "io.mehow.laboratory"

laboratory {
  packageName = "io.mehow.laboratory.sample"

  feature("Authentication") {
    description = "Type of authentication when opening the app"

    withOption("None")
    withOption("Fingerprint")
    withDefaultOption("Retina")
  }

  disabledFeature("LocationTracking") {
    packageName = "io.mehow.laboratory.location"
    isPublic = false
    isRemote = true
  }
}
```

This setup creates two feature flags. `Authentication` and `LocationTracking` with options taken from the `feature(name) { }` and `disabledFeature(name) { }` blocks.

```kotlin
package io.mehow.laboratory.sample

import io.mehow.laboratory.Feature
import kotlin.String

/**
 * Type of authentication when opening the app
 */
public enum class Authentication : Feature<Authentication> {
  None,
  Fingerprint,
  Retina,
  ;

  override val defaultOption: Authentication
    get() = Retina

  override val description: String = "Type of authentication when opening the app"
}
```

```kotlin
package io.mehow.laboratory.location

import io.mehow.laboratory.BinaryFeature
import io.mehow.laboratory.Feature
import kotlin.Boolean

internal enum class LocationTracking(
  override val binaryValue: Boolean,
) : BinaryFeature<LocationTracking> {
  Enabled(true),
  Disabled(false),
  ;

  override val defaultOption: LocationTracking
    get() = Disabled

  override val defaultSource: Feature.Source
    get() = Feature.Source.Remote
}
```

## Feature flags factory

The generation of feature flags factory is useful if you use the [QA module](qa-module.md).

!!! tip
    Check [the samples](https://github.com/MiSikora/laboratory/tree/trunk/samples/) with demo configurations.

```groovy
apply plugin: "io.mehow.laboratory"

laboratory {
  packageName = "io.mehow.laboratory.sample"

  featureFactory()

  feature("FeatureA") {
    withOption("Enabled")
    withDefaultOption("Disabled")
  }

  feature("FeatureB") {
    withOption("Enabled")
    withDefaultOption("Disabled")
  }

  feature("FeatureC") {
    withOption("Enabled")
    withDefaultOption("Disabled")
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

internal fun FeatureFactory.Companion.generated(): FeatureFactory = GeneratedFeatureFactory

private object GeneratedFeatureFactory : FeatureFactory {
  @Suppress("UNCHECKED_CAST")
  override fun create(): Set<Class<out Feature<*>>> = setOf(
    Class.forName("io.mehow.laboratory.sample.FeatureA"),
    Class.forName("io.mehow.laboratory.sample.FeatureB"),
    Class.forName("io.mehow.laboratory.sample.FeatureC")
  ) as Set<Class<Feature<*>>>
}
```

## Feature flag option factory

The generation of an option factory is useful when you want to control local feature flag options remotely. Option factory aggregates all feature flags and recognizes them either by a fully qualified class name or an optional `key` property on a feature flag. Keys must be unique and cannot match fully qualified class names of other feature flags.

![type:video](https://www.youtube.com/embed/yNm2DUcZ0L4)

```groovy
apply plugin: "io.mehow.laboratory"

laboratory {
  packageName = "io.mehow.laboratory.sample"

  optionFactory()

  feature("FeatureA") {
    key = "FeatureA"

    withOption("OptionA")
    withDefaultOption("OptionB")
  }

  feature("FeatureB") {
    withOption("OptionA")
    withDefaultOption("OptionB")
  }

  enabledFeature("FeatureC")
}
```

This uses the `generateOptionFactory` Gradle task that generates the code below.

```kotlin
package io.mehow.laboratory.sample

import io.mehow.laboratory.Feature
import io.mehow.laboratory.OptionFactory
import kotlin.Boolean
import kotlin.String

internal fun OptionFactory.Companion.generated(): OptionFactory = GeneratedOptionFactory

private object GeneratedOptionFactory : OptionFactory {
  override fun create(key: String, name: String): Feature<*>? = when (key) {
    "FeatureA" -> when (name) {
      "OptionA" -> FeatureA.OptionA
      "OptionB" -> FeatureA.OptionB
      else -> null
    }
    "io.mehow.laboratory.sample.FeatureB" -> when (name) {
      "OptionA" -> FeatureB.OptionA
      "OptionB" -> FeatureB.OptionB
      else -> null
    }
    "io.mehow.laboratory.sample.FeatureC" -> when (name) {
      "Enabled" -> FeatureC.Enabled
      "Disabled" -> FeatureC.Disabled
      else -> null
    }
    else -> null
  }

  override fun create(key: String, binaryValue: Boolean): Feature<*>? = when (key) {
    "io.mehow.laboratory.sample.FeatureC" -> if (binaryValue) FeatureC.Enabled else FeatureC.Disabled
    else -> null
  }
}
```

## Multi-module support

The Gradle plugin was written with support for multi-module projects in mind.

!!! tip
    Check [the sample](https://github.com/MiSikora/laboratory/tree/trunk/samples/multi-module) with demo configuration.

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

A Laboratory setup for a Gradle project like above could look like this. Configuration of the Android Gradle plugin or any other dependencies is omitted for brevity.

```groovy
// module-a
plugins {
  id "org.jetbrains.kotlin.jvm"
  id "io.mehow.laboratory"
}

laboratory {
  packageName = "com.sample.a"

  feature("Authentication") {
    withDefaultOption("Password")
    withOption("Fingerprint")
    withOption("Retina")
    withOption("Face")
  }

  feature("AllowScreenshots") {
    withOption("Enabled")
    withDefaultOption("Disabled")
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

    withDefaultOption("Euclidean")
    withOption("Jaccard")
    withOption("Cosine")
    withOption("Edit")
    withOption("Hamming")
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
  id "io.mehow.laboratory"
}

laboratory {
  packageName = "com.sample"
  featureFactory()

  dependency(project(":module-a"))
  dependency(project(":module-b"))
}

dependencies {
  implementation project(":module-b")
}
```

This setup shows that each module can define its feature flags that do not have to be exposed outside. In this scenario, `module-app` is responsible only for gluing together all feature flags so that `Laboratory` instances are aware of feature flag sources and the [QA module](qa-module.md). It should then deliver the correct `Laboratory` to modules via dependency injection. In order to include feature flags during generation of factories, their modules need to be added with `dependency` function.

## Full configuration

Below is the full configuration of the Gradle plugins.

```groovy
laboratory {
  // Sets namespace of generated features and factories. Empty by default.
  packageName = "io.mehow.sample"

  // Informs plugin to create 'enum class SomeFeature' during the generation period.
  feature("SomeFeature") {
    // Used for option factory lookup. No value by default.
    key = "SomeFeatureKey"

    // Overrides globally declared namespace. No value by default.
    packageName = "io.mehow.sample.feature"

    // Adds a description to this feature that can be used for more context.
    description = "Feature description"

    // Sets the visibility of a feature flag to be either 'public' or 'internal'. 'true' by default.
    isPublic = false
    
    // Sets whether the feature flag uses remote source to reads its values. `false` by default.
    isRemote = true

    // Deprecates a feature flag. `DeprecationLevel` argument is optional and uses `DeprecationLevel.Warning` by default.
    // Add the class to the import list in your Gradle script to avoid typing the whole package name.
    deprecated("Deprecation message", io.mehow.laboratory.gradle.DeprecationLevel.Hidden)

    // Informs plugin to add 'ValueA' option to the generated feature flag and set it as a default option.
    // Exactly one of the feature options must be set with this function.
    withDefaultOption("ValueA")

    // Informs plugin to add 'ValueB' option to the generated feature flag.
    withOption("ValueB")
  }

  // Informs plugin to create 'enum class SomeFeature' during the generation period with two options.
  // 'Enabled' and 'Disabled' and uses 'Enabled' as the default one.
  enabledFeature("SomeFeature") {
    // Uses the same options as feature() block except for `withOption()` and `withDefaultOption()`.
  }

  // Informs plugin to create 'enum class SomeFeature' during the generation period with two options.
  // 'Enabled' and 'Disabled' and uses 'Disabled' as the default one.
  disabledFeature("SomeFeature") {
    // Uses the same options as feature() block except for `withOption()` and `withDefaultOption()`.
  }

  // Configures option factory. Useful for integration with remote service such as Firebase.
  optionFactory {
    // Overrides globally declared namespace. No value by default.
    packageName = "io.mehow.sample.factory"

    // Sets visibility of a factory extension function to be either 'public' or 'internal'. 'false' by default.
    isPublic = true
  }

  // Configures feature flags factory. Useful for the QA module configuration.
  featureFactory {
    // Overrides globally declared namespace. No value by default.
    packageName = "io.mehow.sample.factory"

    // Sets visibility of a factory extension function to be either 'public' or 'internal'. 'false' by default.
    isPublic = true
  }

  // Includes feature flags that are used for generation of feature factories and option factory.
  dependency(project(":some-project"))
  // By default dependency contributes to all declared generators but it can be selectively applied
  // by passing a contribution list.
  dependency(project(":some-project"), [DependencyContribution.FeatureFactory, DependencyContribution.OptionFactory])
  // If typesafe project accessors are enabled.
  dependency(projects.someProject)
}
```
