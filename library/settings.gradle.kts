import com.android.build.api.dsl.SettingsExtension

pluginManagement {
  repositories {
    mavenCentral()
    google()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }

  versionCatalogs {
    create("libs") {
      from(files("gradle/dependencies.toml"))
    }
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

include(":laboratory")
include(":shared-preferences")
include(":inspector")
include(":hyperion-plugin")
include(":generator")
include(":gradle-plugin")
include(":data-store")
