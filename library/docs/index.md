# Laboratory ⚗️

![Laboratory](images/laboratory_logo.svg)

A feature flags management library for multi-module Kotlin Android projects. Laboratory offers:

- **Encapsulation**: Different feature flags can freely live in separate Gradle modules without being exposed outside.
- **Type safety**: Feature flags are represented with concrete types instead of booleans or grouped enums.
- **A/B/n testing**: Feature flags don't have to be constrained to provide only a binary choice. They can have as many states as you'd like.
- **Multiple sources**: Feature flags can have different sources used for their current options. For example, you can have a feature flag that takes its option either from a local source or from any number of remote sources like, for instance, Firebase or Azure.
- **QA integration**: Laboratory offers great [QA capabilities](qa-module.md) and easy integration with popular tools. It comes with an out-of-the-box [Hyperion](https://github.com/willowtreeapps/Hyperion-Android) plugin.
- **Persistence ignorance**: Laboratory does not care how you want to store your feature flags. It provides implementations for most common scenarios, but you can always use an implementation for your custom storage mechanism.
- **Testing support**: The in-memory implementation can be used as a drop-in substitute for Laboratory instances in tests.
- **Coroutines support**: Changes to feature flags can be observed via [`Flow`](https://kotlinlang.org/docs/reference/coroutines/flow.html). Options reads and writes are made with `suspend` functions, but you can always opt-in to a blocking equivalent of I/O functions.

## TLDR

First, you need to define your feature flags.

```kotlin
enum class AuthType : Feature<AuthType> {
  None,
  Fingerprint,
  Retina,
  Face;

  public override val defaultOption get() = Fingerprint
}
```

Once you have your feature flags defined, you can start using them in the application.

```kotlin
suspend fun main() {
  // A high-level API for interaction with feature flags
  val laboratory = Laboratory.inMemory()

  // Set AuthType option to Fingerprint
  val success = laboratory.setOption(AuthType.Fingerprint)

  // Check what is the current option of AuthType
  val currentAuthType = laboratory.experiment<AuthType>()

  // Check if the current option of AuthType is equal to Face
  val isFaceAuth = laboratory.experimentIs(AuthType.Face)

  // Observe changes to the AuthType feature flag
  laboratory.observe<AuthType>()
      .onEach { option -> println("AuthType: $option") }
      .launchIn(GlobalScope)
}
```

## Requirements

Laboratory requires [Java 8 bytecode](https://developer.android.com/studio/write/java8-support) support. You can enable it with the following configuration in a `build.gradle` file.

```groovy
android {
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_17
    targetCompatibility JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
  }
}
```

Also, you have to enable [default methods generation](https://blog.jetbrains.com/kotlin/2020/07/kotlin-1-4-m3-generating-default-methods-in-interfaces/) by Kotlin compiler. You can do this by adding a compiler flag in a `build.gradle` file.

```groovy
android {
  kotlinOptions {
    freeCompilerArgs += "-Xjvm-default=all"
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
  implementation "io.mehow.laboratory:laboratory:1.0.3"
}
```

Snapshots of the development version are available on [Sonatype's snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/io/mehow/laboratory/).

Here is the list of all available artifacts that Laboratory library provides.

- **`io.mehow.laboratory:laboratory:1.0.3`**: Core of the library. Defines classes and interfaces that you can interact with from your application code. It also provides R8 rules.
- **`io.mehow.laboratory:laboratory-shared-preferences:1.0.3`**: Provides implementation of `FeatureStorage` based on [`SharedPreferences`](https://developer.android.com/reference/android/content/SharedPreferences).
- **`io.mehow.laboratory:laboratory-data-store:1.0.3`**: Provides implementation of `FeatureStorage` based on [Jetpack `DataStore`](https://developer.android.com/topic/libraries/architecture/datastore).
- **`io.mehow.laboratory:laboratory-inspector:1.0.3`**: QA module that allows users to preview all features and change them at runtime from one place.
- **`io.mehow.laboratory:laboratory-hyperion-plugin:1.0.3`**: QA module that integrates `laboratory-inspector` with [Hyperion](https://github.com/willowtreeapps/Hyperion-Android).
- **`io.mehow.laboratory:laboratory-gradle-plugin:1.0.3`**: Gradle plugin for feature flags generation and other quality of life improvements. It is highly recommended to use it instead of manual class management.

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
