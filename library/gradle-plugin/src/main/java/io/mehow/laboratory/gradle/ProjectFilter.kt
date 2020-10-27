package io.mehow.laboratory.gradle

import org.gradle.api.HasImplicitReceiver
import org.gradle.api.Project

/**
 * Filter for projects that should not contribute their feature flags to the code generation
 * in a merging module.
 */
@HasImplicitReceiver
public fun interface ProjectFilter {
  /**
   * A predicate that excludes [project] from code generation contribution if it returns `true`.
   */
  public fun reject(project: Project): Boolean
}
