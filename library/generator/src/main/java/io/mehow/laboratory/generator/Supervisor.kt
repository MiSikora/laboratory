package io.mehow.laboratory.generator

public class Supervisor(
  internal val featureFlag: FeatureFlagModel,
  internal val option: FeatureFlagOption,
) {
  init {
    require(featureFlag.options.map(FeatureFlagOption::name).contains(option.name)) {
      "Feature flag $featureFlag does not contain option ${option.name}"
    }
  }
}
