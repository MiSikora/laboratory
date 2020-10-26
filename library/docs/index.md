# Laboratory ⚗️

![Laboratory](images/laboratory_logo.svg)

A feature flags management library for multi-module Kotlin Android projects. Laboratory offers:

- **Encapsulation**: Different feature flags can freely live in separate Gradle modules without being exposed outside.
- **Type safety**: Feature flags are represented with concrete types instead of booleans or grouped enums.
- **A/B/C testing**: Feature flags don't have to be constrained to provide only a binary choice. They can have as many states as you'd like.
- **Multiple sources**: Feature flags can have different sources used for their current values. For example, you can have a feature flag that takes its value either from a local source or from any number of remote sources like i.e. Firebase or Azure.
- **QA integration**: Laboratory offers great [QA capabilities](qa-module.md) and easy integration with popular tools. It comes with an out-of-the-box [Hyperion](https://github.com/willowtreeapps/Hyperion-Android) plugin.
- **Persistence ignorance**: Laboratory does not care how you want to store your feature flags. It provides implementations for most common scenarios but you can always use an implementation for your custom storage mechanism.
- **Testing support**: The in-memory implementation can be used as a drop-in substitute of Laboratory instances in tests.
- **Coroutines support**: Changes to feature flags can be observed via [`Flow`](https://kotlinlang.org/docs/reference/coroutines/flow.html). Single value reads and writes are made with `suspend` functions, but you can always opt-in to a blocking equivalent of I/O functions.

## TLDR

First you need to define your feature flags.

```kotlin
enum class AuthType(
  override val isDefaultValue: Boolean = false,
) : Feature<AuthType> {
  None(isDefaultValue = true),
  Fingerprint,
  Retina,
  Face
}
```

Once you have your feature flags defined you can start using them in the application.

```kotlin
suspend fun main() {
  // A high-level API for interaction with feature flags
  val laboratory = Laboratory.inMemory()

  // Set AuthType value to Fingerprint
  laboratory.setFeature(AuthType.Fingerprint)

  // Check what is the current value of AuthType
  val currentAuthType = laboratory.experiment<AuthType>()

  // Check if the current value of AuthType is equal to Face
  val isFaceAuth = laboratory.experimentIs(AuthType.Face)

  // Observe changes to the AuthType feature flag
  laboratory.observe<AuthType>
      .onEach { value -> println("AuthType: $value") }
      .launchIn(GlobalScope)
}
```

## Requirements

Laboratory requires [Java 8 bytecode](https://developer.android.com/studio/write/java8-support) support. You can enable it with the following configuration in a `build.gradle` file.

```groovy
android {
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = "1.8"
  }
}
```

Also, you have to enable [default methods generation](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-default/) by Kotlin compiler. You can do this by adding a compiler flag in a `build.gradle` file.

```groovy
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile) {
  kotlinOptions {
    freeCompilerArgs += "-Xjvm-default=enable"
  }
}
```

## R8

Laboratory ships with R8 rules and doesn't require any extra configuration.

## Get Laboratory

Laboratory is published to [Maven Central Repository](https://search.maven.org/search?q=io.mehow.laboratory).

```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation "io.mehow.laboratory:laboratory:0.7.0"
}
```

Snapshots of the development version are available on [Sonatype's snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/io/mehow/laboratory/).

Here is the list of all available artifacts that Laboratory library provides.

- **`io.mehow.laboratory:laboratory:0.7.0`**: Core of the library. Defines classes and interfaces that you can interact with in your application code. It also provides R8 rules.
- **`io.mehow.laboratory:laboratory-shared-preferences:0.7.0`**: Provides implementation of `FeatureStorage` based on [`SharedPreferences`](https://developer.android.com/reference/android/content/SharedPreferences).
- **`io.mehow.laboratory:laboratory-data-store:0.7.0`**: Provides implementation of `FeatureStorage` based on [Jetpack `DataStore`](https://developer.android.com/topic/libraries/architecture/datastore).
- **`io.mehow.laboratory:laboratory-inspector:0.7.0`**: QA module that allows users to preview all features and change them at runtime from one place.
- **`io.mehow.laboratory:laboratory-hyperion-plugin:0.7.0`**: QA module that integrates `laboratory-inspector` with [Hyperion](https://github.com/willowtreeapps/Hyperion-Android).
- **`io.mehow.laboratory:laboratory-gradle-plugin:0.7.0`**: Gradle plugin for feature flags generation and other quality of life improvements. It is highly recommended to use it instead of manual class management.
- **`io.mehow.laboratory:laboratory-generator:0.7.0`**: Low-level generator of feature flags and any additional classes. You should use this module only if you plan to build your own generation mechanism similar to Gradle plugin.

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

