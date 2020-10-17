plugins {
  kotlin("jvm")
  id("io.mehow.laboratory")
}

laboratory {
  packageName = "io.mehow.laboratory.c"

  feature("DistanceAlgorithm") {
    description = "Algorithm used for destination distance calculations"

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
  api("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.10")
}
