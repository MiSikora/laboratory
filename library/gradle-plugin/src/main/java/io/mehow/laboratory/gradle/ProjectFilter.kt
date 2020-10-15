package io.mehow.laboratory.gradle

import org.gradle.api.HasImplicitReceiver
import org.gradle.api.Project

@HasImplicitReceiver
fun interface ProjectFilter {
  fun reject(project: Project): Boolean
}
