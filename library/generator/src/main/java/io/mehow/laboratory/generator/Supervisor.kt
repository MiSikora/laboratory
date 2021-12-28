package io.mehow.laboratory.generator

public class Supervisor(
  public val featureFlag: FeatureFlagModel,
  public val option: FeatureFlagOption,
) {
  init {
    require(featureFlag.options.map(FeatureFlagOption::name).contains(option.name)) {
      "Feature flag $featureFlag does not contain option ${option.name}"
    }
  }
}
