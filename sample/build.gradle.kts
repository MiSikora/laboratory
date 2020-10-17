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
    description = "Reports during cold start whether device is rooted"

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
  api("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.10")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
  implementation("com.google.android.material:material:1.2.1")
  implementation("com.willowtreeapps.hyperion:hyperion-core:0.9.30")
  implementation("io.mehow.laboratory:hyperion-plugin")
  implementation("io.mehow.laboratory:shared-preferences")
  implementation(project(":sample:module-a"))
  implementation(project(":sample:module-b"))
  implementation(project(":sample:module-c"))
}
