package io.mehow.laboratory.gradle;

import org.gradle.api.HasImplicitReceiver;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

@HasImplicitReceiver // Does not work with Kotlin defined interfaces in *.kts build files.
public interface ProjectFilter {
  boolean reject(@NotNull Project project);
}
