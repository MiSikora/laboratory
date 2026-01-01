import com.vanniktech.maven.publish.MavenPublishBaseExtension

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

tasks.dokkaHtmlMultiModule {
  moduleName.set("Laboratory")
  moduleVersion.set(project.property("VERSION_NAME") as String)
  outputDirectory.set(rootDir.resolve("docs/api"))
}

val mavenPublishId = libs.plugins.maven.publish.get().pluginId

subprojects {
  pluginManager.withPlugin(mavenPublishId) {
    configure<MavenPublishBaseExtension> {
      publishToMavenCentral()
      signAllPublications()
    }
  }
}
