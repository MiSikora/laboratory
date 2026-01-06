plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.agp.library) apply false
  alias(libs.plugins.maven.publish) apply false
  alias(libs.plugins.binary.compatibility.validator)
  alias(libs.plugins.dokka)
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.wire) apply false
  alias(libs.plugins.ksp) apply false
  id("io.mehow.laboratory.convention")
}

apiValidation {
  ignoredProjects.add("testing")
  ignoredPackages.add("io.mehow.laboratory.internal")
  nonPublicMarkers.add("io.mehow.laboratory.internal.InternalLaboratoryApi")
}

tasks.dokkaHtmlMultiModule {
  moduleName.set("Laboratory")
  moduleVersion.set(project.name)
  outputDirectory.set(rootDir.resolve("docs/api"))
}
