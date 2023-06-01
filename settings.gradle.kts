import com.android.build.api.dsl.SettingsExtension

pluginManagement {
  repositories {
    mavenCentral()
    google()
  }
}

plugins {
  id("com.android.settings") version "8.0.2"
}

@Suppress("UnstableApiUsage")
extensions.getByType(SettingsExtension::class).apply {
  compileSdk = 33
  minSdk = 21
}

include(":samples:basic")
include(":samples:default-option")
include(":samples:multi-module")
include(":samples:multi-module:multi-module-a")
include(":samples:multi-module:multi-module-b")
include(":samples:multi-module:multi-module-c")
include(":samples:firebase")
include(":samples:supervision")
include(":samples:ci-check")

includeBuild("library")
