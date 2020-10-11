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
  sourcedStorage()
  featureFactory()

  feature("LogType") {
    withValue("Verbose")
    withValue("Debug")
    withDefaultValue("Info")
    withValue("Warning")
    withValue("Error")
  }

  feature("ReportRootedDevice") {
    withValue("Enabled")
    withDefaultValue("Disabled")
  }

  feature("ShowAds") {
    withDefaultValue("Enabled")
    withValue("Disabled")

    withSource("Azure")
  }
}

dependencies {
  api(Libs.Kotlin.StdLibJdk7)
  implementation(Libs.Kotlin.Coroutines.Android)
  implementation(Libs.Material)
  implementation(Libs.Hyperion.Core)
  @Suppress("GradleDynamicVersion") // We want the latest version as we control it.
  implementation("io.mehow.laboratory:laboratory-hyperion-plugin:+")
  @Suppress("GradleDynamicVersion") // We want the latest version as we control it.
  implementation("io.mehow.laboratory:laboratory-shared-preferences:+")
  implementation(project(":sample:module-a"))
  implementation(project(":sample:module-b"))
  implementation(project(":sample:module-c"))
}
