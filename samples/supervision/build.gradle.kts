plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.laboratory)
  alias(libs.plugins.detekt)
  alias(libs.plugins.spotless)
}

android {
  namespace = "io.mehow.laboratory.sample.supervision"
}

laboratory {
  packageName = "io.mehow.laboratory.sample.supervision"

  featureFactory()

  feature("Theming") {
    withDefaultOption("Default")

    withOption("Christmas") {
      feature("ChristmasGreeting") {
        withDefaultOption("Disabled")
        withOption("Hello")
        withOption("HoHoHo")
      }

      feature("ChristmasBackground") {
        withDefaultOption("Disabled")
        withOption("Reindeer")
        withOption("Snowman")
      }
    }

    withOption("Halloween") {
      feature("SpookyMusic") {
        withDefaultOption("Disabled")
        withOption("Graveyard")
        withOption("HauntedHouse")
        withOption("OldCastle")
      }

      feature("WitchChance") {
        withDefaultOption("Chance00")
        withOption("Chance20")
        withOption("Chance50")
        withOption("Chance100")
      }

      feature("CandyArt") {
        withDefaultOption("Disabled")
        withOption("ChocolateGhost")
        withOption("MarshmallowGhost")
      }
    }
  }
}

dependencies {
  implementation(libs.kotlinx.coroutinesAndroid)
  implementation(libs.android.material)
  implementation(libs.hyperion.core)
  implementation(libs.laboratory.dataStore)
  implementation(libs.laboratory.hyperionPlugin)
}
