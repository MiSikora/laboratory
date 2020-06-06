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
}
