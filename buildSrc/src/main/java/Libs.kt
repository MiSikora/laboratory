object Libs {
  const val AndroidGradlePlugin = "com.android.tools.build:gradle:4.0.0"

  object Kotlin {
    const val Version = "1.3.72"

    const val GradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$Version"

    const val StdLibJdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$Version"
  }

  object Kotest {
    const val Version = "4.0.6"

    const val RunnerJunit5 = "io.kotest:kotest-runner-junit5-jvm:$Version"

    const val Assertions = "io.kotest:kotest-assertions-core-jvm:$Version"
  }

  object AndroidX {
    const val CoreKtx = "androidx.core:core-ktx:1.3.0"

    object Test {
      const val Version = "1.2.0"

      const val CoreKtx = "androidx.test:core-ktx:$Version"

      const val Orchestrator = "androidx.test:orchestrator:$Version"

      const val Runner = "androidx.test:runner:$Version"
    }

    const val TestExtJUnitKtx = "androidx.test.ext:junit-ktx:1.1.1"
  }

  const val Material = "com.google.android.material:material:1.2.0-beta01"

  const val HyperionPlugin = "com.willowtreeapps.hyperion:hyperion-plugin:0.9.27"

  const val AutoService = "com.google.auto.service:auto-service:1.0-rc6"

  const val MavenPublishGradlePlugin = "com.vanniktech:gradle-maven-publish-plugin:0.11.1"

  object Detekt {
    const val Version = "1.9.1"

    const val GradlePluginId = "io.gitlab.arturbosch.detekt"

    const val GradlePlugin = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$Version"

    const val Formatting = "io.gitlab.arturbosch.detekt:detekt-formatting:$Version"

    const val Cli = "io.gitlab.arturbosch.detekt:detekt-cli:$Version"
  }
}
