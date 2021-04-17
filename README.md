# Laboratory ⚗️

![Quality Check CI](https://github.com/MiSikora/laboratory/workflows/Quality%20Check/badge.svg?branch=trunk&event=push)
![Snapshot CI](https://github.com/MiSikora/laboratory/workflows/Snapshot/badge.svg?branch=trunk&event=push)
[<img src="https://img.shields.io/maven-central/v/io.mehow.laboratory/laboratory.svg?label=latest%20release"/>](https://search.maven.org/search?q=g:io.mehow.laboratory)
[<img src="https://img.shields.io/nexus/s/https/oss.sonatype.org/io.mehow.laboratory/laboratory.svg?label=latest%20snapshot"/>](https://oss.sonatype.org/content/repositories/snapshots/io/mehow/laboratory/)
![GitHub License](https://img.shields.io/github/license/MiSikora/laboratory)

Feature flags for multi-module Kotlin Android projects.

Please visit [project website](https://mehow.io/laboratory/) for the full documentation and the [changelog](https://mehow.io/laboratory/changelog/).

## TLDR

Add Laboratory dependency to your project.

```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation "io.mehow.laboratory:laboratory:0.12.1"
}
```

Enable Java 11 support.

```groovy
android {
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_11
    targetCompatibility JavaVersion.VERSION_11
  }

  kotlinOptions {
    jvmTarget = "11"
    freeCompilerArgs += "-Xjvm-default=enable"
  }
}
```

Define your feature flags.

```kotlin
enum class AuthType : Feature<AuthType> {
  None,
  Fingerprint,
  Retina,
  Face;

  public override val defaultOption get() = Fingerprint
}
```

Start using them in the application.

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
