import com.android.build.api.variant.VariantFilter

plugins {
  id("com.android.application")
  kotlin("android")
  id("io.mehow.laboratory")
}

android {
  signingConfigs {
    register("config") {
      keyAlias = "mehow-io"
      keyPassword = "mehow-io"
      storeFile = file("mehow-io.keystore")
      storePassword = "mehow-io"
    }
  }

  defaultConfig {
    applicationId = "io.mehow.laboratory.sample"

    versionCode = 1
    versionName = "1.0.0"

    signingConfig = signingConfigs.getByName("config")
  }

  buildTypes {
    named("debug") {
      setMatchingFallbacks("release")
    }
  }

  variantFilter = Action<VariantFilter> {
    ignore = name != "debug"
  }
}

laboratory {
  packageName = "io.mehow.laboratory.sample"
  factory()
}

dependencies {
  implementation(Libs.Kotlin.StdLibJdk7)
  implementation(Libs.Material)
  implementation(Libs.Hyperion.Core)
  @Suppress("GradleDynamicVersion") // We want the latest version as we control it.
  implementation("io.mehow.laboratory:laboratory-hyperion-plugin:+")
  implementation(project(":sample:brombulator"))
  implementation(project(":sample:frombulator"))
  implementation(project(":sample:trombulator"))
}
