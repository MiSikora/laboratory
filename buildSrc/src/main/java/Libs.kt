object Libs {
  const val AndroidGradlePlugin = "com.android.tools.build:gradle:4.1.0"

  object Kotlin {
    const val Version = "1.4.10"

    const val GradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$Version"

    const val StdLibJdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$Version"

    const val StdLibJdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$Version"

    const val DokkaGradlePlugin = "org.jetbrains.dokka:dokka-gradle-plugin:$Version"

    object Coroutines {
      const val Version = "1.3.9"

      const val Core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$Version"

      const val Android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$Version"
    }
  }

  object Kotest {
    const val Version = "4.3.0"

    const val RunnerJunit5 = "io.kotest:kotest-runner-junit5-jvm:$Version"

    const val Assertions = "io.kotest:kotest-assertions-core-jvm:$Version"

    const val AssertionsArrow = "io.kotest:kotest-assertions-arrow:$Version"

    const val Property = "io.kotest:kotest-property-jvm:$Version"
  }

  object AndroidX {
    const val CoreKtx = "androidx.core:core-ktx:1.3.2"

    const val AppCompat = "androidx.appcompat:appcompat:1.2.0"

    const val DataStore = "androidx.datastore:datastore-core:1.0.0-alpha02"

    const val FragmentKtx = "androidx.fragment:fragment-ktx:1.2.5"

    const val ViewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0"

    const val ViewPager2 = "androidx.viewpager2:viewpager2:1.0.0"

    const val RecyclerView = "androidx.recyclerview:recyclerview:1.1.0"

    object Test {
      const val Version = "1.3.0"

      const val CoreKtx = "androidx.test:core-ktx:$Version"

      const val Orchestrator = "androidx.test:orchestrator:$Version"

      const val Runner = "androidx.test:runner:$Version"
    }

    const val TestExtJUnitKtx = "androidx.test.ext:junit-ktx:1.1.2"
  }

  const val Material = "com.google.android.material:material:1.2.1"

  object Hyperion {
    const val Version = "0.9.30"

    const val Plugin = "com.willowtreeapps.hyperion:hyperion-plugin:$Version"

    const val Core = "com.willowtreeapps.hyperion:hyperion-core:$Version"
  }

  const val AutoService = "com.google.auto.service:auto-service:1.0-rc7"

  const val MavenPublishGradlePlugin = "com.vanniktech:gradle-maven-publish-plugin:0.13.0"

  object Detekt {
    const val Version = "1.14.1"

    const val GradlePluginId = "io.gitlab.arturbosch.detekt"

    const val GradlePlugin = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$Version"

    const val Formatting = "io.gitlab.arturbosch.detekt:detekt-formatting:$Version"

    const val Cli = "io.gitlab.arturbosch.detekt:detekt-cli:$Version"
  }

  object GradleVersions {
    const val Version = "0.33.0"

    const val GradlePluginId = "com.github.ben-manes.versions"

    const val GradlePlugin = "com.github.ben-manes:gradle-versions-plugin:$Version"
  }

  const val KotlinPoet = "com.squareup:kotlinpoet:1.7.1"

  const val Arrow = "io.arrow-kt:arrow-core:0.10.5"

  object Wire {
    const val Version = "3.4.0"

    const val Runtime = "com.squareup.wire:wire-runtime:$Version"

    const val GradlePlugin = "com.squareup.wire:wire-gradle-plugin:$Version"
  }

  const val Turbine = "app.cash.turbine:turbine:0.2.1"
}
