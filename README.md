# Laboratory

![Quality Check CI](https://github.com/MiSikora/Laboratory/workflows/Quality%20Check/badge.svg?branch=master&event=push)
![Snapshot CI](https://github.com/MiSikora/Laboratory/workflows/Snapshot/badge.svg?branch=master&event=push)
[<img src="https://img.shields.io/maven-central/v/io.mehow.laboratory/laboratory.svg?label=latest%20release"/>](https://search.maven.org/search?q=g:io.mehow.laboratory)
[<img src="https://img.shields.io/nexus/s/https/oss.sonatype.org/io.mehow.laboratory/laboratory.svg?label=latest%20snapshot"/>](https://oss.sonatype.org/content/repositories/snapshots/io/mehow/laboratory/)
![GitHub License](https://img.shields.io/github/license/MiSikora/Laboratory)

Feature flags for multi-module Kotlin Android projects.

## Overview

Laboratory simplifies feature flags access and management using a single interface.

```kotlin
enum class AuthMethod(override val isDefaultValue: Boolean = false) : Feature<AuthMethod> {
  None(isDefaultValue = true),
  Fingerprint,
  Face
}

class IntroductionPresenter(private val laboratory: Laboratory) {
  // Read feature from the laboratory.
  suspend fun onIntroductionFinished() = when(laboratory.experiment<AuthFeature>()) {
    AuthMethod.None -> TODO("Go to the main screen")
    AuthMethod.Fingerprint -> TODO("Show fingerprint scanner")
    AuthMethod.Face -> TODO("Show face scanner")
  }
}

class SettingsPresenter(private val laboratory: Laboratory) {
  // Change feature value.
  suspend fun setAuthMethod(method: AuthMethod) = laboratory.setFeature(method)
}
```

By default, if a feature is not set, the first enum value that has has `isDefaultValue` property set to `true` is used. If no value has this property set to `true` then first enum is used as a default. `Laboratory` instances delegate their work to a `FeatureStorage` interface that you generally do not have to use in your application code.

| Artifact                                                          | Description                                                                                                                                     |
| ----------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| `io.mehow.laboratory:laboratory:0.6.1`                            | Core of the library. Defines classes and interfaces that you should interact with. It also provides a `Laboratory.inMemory()` method for tests. |
| `io.mehow.laboratory:laboratory-shared-preferences:0.6.1`         | Provides implementation of `FeatureStorage` based on `SharedPreferences` and adds R8 rules for features.                                        |
| `io.mehow.laboratory:laboratory-data-store:0.6.1`                 | Provides implementation of `FeatureStorage` based on Jetpack `DataStore` and adds R8 rules for features.                                        |
| `io.mehow.laboratory:laboratory-inspector:0.6.1`                  | QA module that allows to preview all features and change them at runtime from one place.                                                        |
| `io.mehow.laboratory:laboratory-hyperion-plugin:0.6.1`            | QA module that integrates `laboratory-inspector` with the [Hyperion](https://github.com/willowtreeapps/Hyperion-Android).                       |
| `io.mehow.laboratory:laboratory-gradle-plugin:0.6.1`              | Gradle plugin for feature management. It is highly recommended to be used when `laboratory-inspector` is availalbe in the application.          |
| `io.mehow.laboratory:laboratory-generator:0.6.1`                  | Generates feature flags and feature flag factory classes.                                                                                       |

## QA support

Often, it is a good practice to have a way of changing features at runtime. Laboratory helps with that with its QA artifacts.

`laboratory-inspector` provides `LaboratoryActivity` that can show a preview of all features with a possibility to modify them. However, it requires some upfront initialization. The place to do this is your application class. After initialization you can open the activity to interact with the features.

```kotlin
class SampleApp : Application() {
  override fun onCreate() {
    super.onCreate()
    val factory = TODO("Get factory.")
    val laboratory = TODO("Get laboratory.")
    LaboratoryActivity.configure(laboratory, factory)
  }
}

// Somewhere in the application
fun openLaboratory(context: Context) {
  LaboratoryActivity.start(context)
}
```

A `FeatureStorage` persistence mechanism should come from the DI graph and must be shared with `Laboratory` instances to keep changes in sync. A `FeatureFactory` should return all enum classes that should be used as features. You can provide your custom implementation but it should be easier to rely on `laboratory-gradle-plugin` to do this job.

| Inspector                 | Hyperion                 |
|:-------------------------:|:------------------------:|
| ![](images/inspector.jpg) | ![](images/hyperion.jpg) |

## Gradle plugin

### Basic setup

You can use Laboratory without Gradle plugin but if you intend to integrate it with any of the available QA or advanced features it is better to automate your work with the plugin. The plugin is responsible for generating feature enums, generating `FeatureFactory` that provides features generated by the plugin in any module, and for adding `laboratory` dependency to the module.

Here is a sample plugin configuration.

```groovy
plugins {
  id("org.jetbrains.kotlin.jvm")
  id("io.mehow.laboratory")
}

laboratory {
  packageName = "io.mehow.sample"

  featureFactory()

  feature("Brombulation") {
    withDefaultValue("Enabled")
    withValue("Disabled")
  }

  feature("Frombulation") {
    withDefaultValue("Enabled")
    withValue("Disabled")
  }

  feature("Trombulation") {
    withValue("LevelOne")
    withDefaultValue("LevelTwo")
    withValue("LevelThree")
  }
}
```

This configuration adds two Gradle tasks (`generateFeatureFlags` and `generateFeatureFactory`) that will run before compilation. They are responsible for generating the following classes.

```kotlin
package io.mehow.sample

// Generated by 'generateFeatureFlags'

enum class Brombulation(override val isDefaultValue: Boolean = false) : Feature<Brombulation> {
  Enabled(isDefaultValue = true),
  Disabled
}

enum class Frombulation(override val isDefaultValue: Boolean = false) : Feature<Frombulation> {
  Enabled(isDefaultValue = true),
  Disabled
}

enum class Trombulation(override val isDefaultValue: Boolean = false) : Feature<Trombulation> {
  LevelOne,
  LevelTwo(isDefaultValue = true),
  LevelThree
}

// Generated by 'generateFeatureFactory'.

internal fun FeatureFactory.Companion.featureGenerated(): FeatureFactory = GeneratedFeatureFactory

private object GeneratedFeatureFactory : FeatureFactory {
  @Suppress("UNCHECKED_CAST")
  override fun create() = setOf(
    Class.forName("io.mehow.sample.Brombulation"),
    Class.forName("io.mehow.sample.Frombulation"),
    Class.forName("io.mehow.sample.Trombulation")
  ) as Set<Class<Feature<*>>>
}
```

`GeneratedFeatureFactory` does not use feature classes directly as they might be internal to the contributing modules or available only as transitive dependencies.

### Advanced setup

Laboratory allows providing different data sources for each feature independently. This allows you to easily switch between i.e. local configuration of features for QA purposes and having them configured with different remote sources.

This functionality can be enabled by `sourcedWith` property on a `Feature` interface and by using customized `FeatureStorage.sourced()` storage. Sample setup could look like this.

```kotlin
enum class PowerPlant(override val isDefaultValue: Boolean = false) : Feature<PowerPlant> {
  Coal,
  Solar(isDefaultValue = true),
  ColdFusion,
  ;

  @Suppress("UNCHECKED_CAST")
  override val sourcedWith = Source::class.java as Class<Feature<*>>

  enum class Source(override val isDefaultValue: Boolean = false): Feature<Source> {
    Local,
    Firebase,
    Aws(isDefaultValue = true),
    ;
  }
}

val localStorage: FeatureStorage = TODO("Create local storage")
val firebaseStorage: FeatureStorage = TODO("Create Firebase storage")
val awsStorage: FeatureStorage = TODO("Create AWS storage")
val storage = FeatureStorage.sourced(
  localSource = localStorage,
  remoteSources = mapOf(
    "Firebase" to firebaseStorage,
    "Aws" to awsStorage,
  ),
)
val laboratory = Laboratory(storage)
```

While there is no magic to this code and it can be written manually it can very quickly get tedious and it is much easier to delegate this task to the Gradle plugin.

```groovy
laboratory {
  feature("PowerPlant") {
    withValue("Coal")
    withDefaultValue("Solar")
    withValue("ColdFusion")

    withSource("Firebase")
    withDefaultSource("Aws")
  }
}
```

Notice that you don't have to specify `Local` source in the configuration. It is because 'Local' source is always required and generated if any source is present on a feature flag. The snippet above will generate the following code.

```kotlin
// Generated by 'generateFeatureFlags'

enum class PowerPlant(override val isDefaultValue: Boolean = false) : Feature<PowerPlant> {
  Coal,
  Solar(isDefaultValue = true),
  ColdFusion,
  ;

  @Suppress("UNCHECKED_CAST")
  override val sourcedWith = Source::class.java as Class<Feature<*>>

  enum class Source(override val isDefaultValue: Boolean = false): Feature<Source> {
    Local,
    Firebase,
    Aws(isDefaultValue = true),
    ;
  }
}

// Generated by 'generateSourcedFeatureStorage'

internal fun FeatureStorage.Companion.sourcedGenerated(
  localSource: FeatureStorage,
  firebaseSource: FeatureStorage,
  awsSource: FeatureStorage
): FeatureStorage = sourced(
  localSource,
  mapOf(
    "Firebase" to fooSource,
    "Aws" to barSource
  )
)
```

To control sources from the QA module, there is one more extension available. `featureSourceFactory()` extension is responsible for collecting all feature sources and similarly to `featureFactory()` task creating a factory .

### Full configuration

```groovy
// Adds 'generateFeatureFlags' task to the module.
laboratory {
  // Sets namespace of generated features and factories. Empty by default.
  packageName = "io.mehow.sample"

  // Adds 'generateFeatureFactory' task to the module. Optional.
  featureFactory {
    // Overrides globally declared namespace. No value by default.
    packageName = "io.mehow.sample.factory"

    // Sets visibility of a factory extension function to be either 'public' or 'internal'. 'false' by default.
    isPublic = true

    // Excludes projects from contributing features to this factory. Includes all projects by default.
    excludeProjects { project -> true }
  }

  // Informs plugin to create 'enum class SomeFeature' during the generation period.
  feature("SomeFeature") {
    // Overrides globally declared namespace. No value by default.
    packageName = "io.mehow.sample.feature"

    // Adds description to this feature that can be used for more context.
    description = "Feature description"

    // Sets visibility of a feature flag to be either'public' or 'internal'. 'true' by default.
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

  // Adds 'generateSourcedFeatureStorage' task to the module. Optional.
  sourcedStorage {
    // Overrides globally declared namespace. No value by default.
    packageName = "io.mehow.sample.storage"

    // Sets visibility of a storage extension function to be either 'public' or 'internal'. 'false' by default.
    isPublic = true

    // Excludes projects from contributing sources to this storage. Includes all projects by default.
    excludeProjects { project -> true }
  }

  // Adds 'generateFeatureSourceFactory' task to the module. It generates a feature factory for all generates sources. Optional.
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

You can also check [the sample](sample/) for more info.

## Integration

Laboratory requires Java 8 bytecode and enabled `@JvmDefault` annotation. To enable Java 8 desugaring configure it in your Gradle script.

```groovy
android {
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
  // For Kotlin projects
  kotlinOptions {
    jvmTarget = "1.8"
  }
}
```

To enable `@JvmDefault` [add a compiler flag](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-default/).

## License

    Copyright 2020 Michał Sikora

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
