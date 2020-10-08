Change Log
==========

Version 0.5.0 *(2020-10-08)*
----------------------------

* Change 'fallback' nomenclature to 'default'. This affects Gradle plugin `withFallbackValue()` and `withFallbackSource` extensions as well as `isFallbackValue` property on `Feature` interface.

Version 0.4.0 *(2020-10-08)*
----------------------------

* Changed name of generated sourced feature storage function to align it with feature factories.

Version 0.3.0 *(2020-10-08)*
----------------------------

* `factory()` extension is renamed to `featureFactory()`
* Feature flags can have different sources that can be toggled at runtime.
* `feature()` extension enabled setting feature sources with `withSource()` and `withFallbackSource()` functions.
* Gradle plugin has a new `sourcedStorage()` extension. It is responsible for generating a customized `FeatureStorage` based on all available feature sources.
* Gradle plugin has a new `featureSourceFactory()` extension. It works similarly to `featureFactory()` extension except that it collects only feature sources.
* `LaboratoryActivity` is now configured with `configure()` function instead of `initialize()`.
* `LaboratoryActivity` displays features on tabs. This allows to display i.e. features and feature sources separately.

Version 0.2.1 *(2020-10-02)*
----------------------------

* Enable to read and write features with Laboratory in an opt in, blocking way.
* Renamed `laboratory-android` artifact to `laboratory-shared-preferences` artifact.
* `laboratory-shared-preferences` artifact (old `laboratory-android`) is no longer applied by Gradle automatically.

Version 0.2.0 *(2020-09-05)*
----------------------------

* Kotlin standard library is now part of public API.
* `Laboatory` and `FeatureStorage` use now `suspending` functions.
* Added support for observing features via `Flow`.
* Added support for Jetpack DataStore in `laboratory-date-store` artifact.
* `Laboratory` and `FeatureStorage` inform now of failures when setting features. This currently works only with DataStore module.
* `@Feature` annotation is removed. Instead, enums should implement `Feature` interface which allows to define a fallback value. If no fallback is defined first enum is used. If multiple fallbacks are defined the first one in order is used.
* Gradle plugin requires exactly one feature value to be added with `withFallbackValue("name")` function.

Version 0.1.0 *(2020-06-12)*
----------------------------

* Initial release.
