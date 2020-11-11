# Laboratory ⚗️

![Quality Check CI](https://github.com/MiSikora/Laboratory/workflows/Quality%20Check/badge.svg?branch=master&event=push)
![Snapshot CI](https://github.com/MiSikora/Laboratory/workflows/Snapshot/badge.svg?branch=master&event=push)
[<img src="https://img.shields.io/maven-central/v/io.mehow.laboratory/laboratory.svg?label=latest%20release"/>](https://search.maven.org/search?q=g:io.mehow.laboratory)
[<img src="https://img.shields.io/nexus/s/https/oss.sonatype.org/io.mehow.laboratory/laboratory.svg?label=latest%20snapshot"/>](https://oss.sonatype.org/content/repositories/snapshots/io/mehow/laboratory/)
![GitHub License](https://img.shields.io/github/license/MiSikora/Laboratory)

Feature flags for multi-module Kotlin Android projects.

Please visit [project website](https://misikora.github.io/Laboratory/) for the full documentation and the [changelog](https://misikora.github.io/Laboratory/changelog/).

## TLDR

Add Laboratory dependency to your project.

```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation "io.mehow.laboratory:laboratory:0.8.0"
}
```

Enable Java 8 support.

```groovy
android {
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = "1.8"
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

  // Set AuthType value to Fingerprint
  val success = laboratory.setFeature(AuthType.Fingerprint)

  // Check what is the current value of AuthType
  val currentAuthType = laboratory.experiment<AuthType>()

  // Check if the current value of AuthType is equal to Face
  val isFaceAuth = laboratory.experimentIs(AuthType.Face)

  // Observe changes to the AuthType feature flag
  laboratory.observe<AuthType>()
      .onEach { value -> println("AuthType: $value") }
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
