plugins {
  kotlin("jvm")
  id("io.mehow.laboratory")
}

laboratory {
  packageName = "io.mehow.laboratory.c"

  feature("DistanceAlgorithm") {
    withDefaultValue("Euclidean")
    withValue("Jaccard")
    withValue("Cosine")
    withValue("Edit")
    withValue("Hamming")

    withSource("Firebase")
    withDefaultSource("Azure")
  }
}

dependencies {
  api(Libs.Kotlin.StdLibJdk7)
}
