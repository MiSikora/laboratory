import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.agp.library) apply false
  alias(libs.plugins.maven.publish) apply false
  alias(libs.plugins.binary.compatibility.validator)
  alias(libs.plugins.dokka)
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.laboratory.convention)
}

apiValidation {
  ignoredProjects.add("testing")
  ignoredPackages.add("io.mehow.laboratory.internal")
  nonPublicMarkers.add("io.mehow.laboratory.internal.InternalLaboratoryApi")
}

dokka {
  moduleName.set("Laboratory")
  moduleVersion.set(project.name)
  dokkaPublications.html { outputDirectory.set(rootDir.resolve("docs/api")) }
}

subprojects {
  val dokkaId = rootProject.libs.plugins.dokka.get().pluginId
  pluginManager.withPlugin(dokkaId) {
    configure<DokkaExtension>() {
      dokkaSourceSets.configureEach {
        documentedVisibilities.add(VisibilityModifier.Public)
        reportUndocumented.set(true)
        perPackageOption {
          matchingRegex.set(".*\\.internal.*")
          suppress.set(true)
        }
      }
    }
  }
}

dependencies {
  dokka(project(":laboratory:data-store"))
  dokka(project(":laboratory:generator"))
  dokka(project(":laboratory:gradle-plugin"))
  dokka(project(":laboratory:hyperion-plugin"))
  dokka(project(":laboratory:inspector"))
  dokka(project(":laboratory:runtime"))
  dokka(project(":laboratory:shared-preferences"))
}
