# Changelog
All notable changes to this project will be documented in this document.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.8.0] - 2020-10-28
### Added
- KDoc documentation.

### Changed
- Renamed `feature`/`features` argument in `setFeature()` and `setFeatures()` methods to `value`/`values` respectively.
- Elevation is no longer an attribute in the `IoMehowLaboratory.Theme` and a regular resource is used instead. This makes sure that when an Activity theme is overridden externally it won't crash for an unknown attribute.
- Flatten Hyperion button to visually match other items.
- Upgrade to Coroutines `1.4.0`.

### Removed
- `generateFactory` property from `sourcedFeatureStorage()` method in Gradle plugin. It was added to the public API by a mistake and wasn't responsible for anything.
- Wire dependency from the `library-shared-preferences` artifact. It was added by a mistake.
- `BuildConfig` classes from Android library modules.

## [0.7.0] - 2020-10-22
### Changed
- Changelog format follows now [Keep a Changelog](https://keepachangelog.com/) format. Format is applied retroactively to this file.
- R8 rules are now a part of `META-INF` of the `laboratory` artifact.
- `SharedPreferencesFeatureStorage` is now `internal`.
- Gradle plugin no longer has a runtime dependency on Android Gradle Plugin.
- `laboratory-generator` generates source code compatible with the [explicit API mode](https://kotlinlang.org/docs/reference/whatsnew14.html#explicit-api-mode-for-library-authors).
- Set compile SDK to `30`.
- Upgrade to KotlinPoet `1.7.2`.
- Upgrade to Hyperion `0.9.30`.
- Upgrade to DataStore `1.0.0-alpha02`.

## [0.6.2] - 2020-10-12
### Added
- `FeatureStorage` extensions for creation of [`SharedPreferences`](https://developer.android.com/reference/android/content/SharedPreferences) based `FeatureStorage`.
- `FeatureStorage` extensions for creation of [`DataStore`](https://developer.android.com/topic/libraries/architecture/datastore) based `FeatureStorage`.
- [Hyperion](https://github.com/willowtreeapps/Hyperion-Android) plugin can be ordered in the debug menu by overriding `io_mehow_laboratory_plugin_id` resource.

### Deprecated
- `SharedPreferenceFeatureStorage` soon will become `internal`.

### Changed
- `DataStoreFeatureStorage` is now `internal`. It is not considered a breaking change as [`DataStore`](https://developer.android.com/topic/libraries/architecture/datastore) is in the alpha stage.

## [0.6.1] - 2020-10-12
### Fixed
- [Hyperion](https://github.com/willowtreeapps/Hyperion-Android) plugin layout where button was on the wrong side of the debug menu.

## [0.6.0] - 2020-10-12
### Added
- `Feature` can have now description. It can be used to add more contextual data to feature flags.
- `LaboratoryActivity` observes changes to feature flags instead of loading them every time the screen is opened.
- `LaboratoryActivity` displays feature flag sources next to them and allows users to select a source from a drop down menu.
- Remote feature flag values are displayed in `LaboratoryActivity` if a source is not local.
- When a remote source is used for a feature flag a value cannot be changed from `LaboratoryActivity`.
- `LaboratoryActivity` displays feature flag descriptions if they are present.
- `LaboratoryActivity` can reset feature flag values to their default state from an item in the action bar.
- `Laboratory.experimentIs()` and `Laboratory.experimentIsBlocking()` functions that allow to check if a feature flag has particular value.
- ViewPager2 `1.0.0` dependency to `laboratory-inspector`.
- RecyclerView `1.1.0` dependency to `laboratory-inspector`.

### Changed
- `LaboratoryActivity` requires now a `Laboratory` instance for initialization. This `Laboratory` should share `FeatureStorage` with instances of `Laboratory` used in the application.

## [0.5.0] - 2020-10-08
### Changed
- `fallback` nomenclature to `default`. This affects Gradle plugin `withFallbackValue()` and `withFallbackSources()` functions as well as `isFallbackValue` property on the `Feature` interface.

## [0.4.0] - 2020-10-08
### Changed
- Name of the generated sourced `FeatureStorage` extension function is now `sourcedGenerated()` in order to align it with the generated feature factory extension function name.

## [0.3.0] - 2020-10-08
### Added
- Feature flags can have multiple sources. Source is also a feature flag and is optional. If no source is available it is assumed that only a local source is controlled.
- `FeatureStorage` that connects feature flags with their sources. It is available via `FeatureStorage.sourced()` extension function. Feature flag sources are uniquely identified only by their value names.
- Feature flag sources can be set from the Gradle plugin with `withSource("Name")` and `withFallbackSource("Name")` functions in `feature()` blocks. Any source that has the name "Local" (or a variant of it) is filtered out.
- Gradle plugin has a new `sourcedStorage()` function. It is responsible for generating a customized `FeatureStorage` that is aware of all available feature flag sources.
- Gradle plugin has a new `featureSourceFactory()` function. It works similarly to `featureFactory()` function with a difference that it collects only feature flag sources.
- `LaboratoryActivity` is now configurable with the `configure() function`.
- `LaboratoryActivity` can display different sets of feature flags on separate tabs.
- FragmentKtx `1.2.5` dependency to `laboratory-inspector`.
- ViewModelKtx `2.2.0` dependency to `laboratory-inspector`.

### Changed
- `LaboratoryActivity.initialize()` function is renamed to `configure()`.
- Gradle plugin `factory()` function is renamed to `featureFactory()`.

## [0.2.1] - 2020-10-02
### Added
- `Laboratory` exposes a blocking way of reading and writing feature flags. It requires an [opt-in](https://kotlinlang.org/docs/reference/opt-in-requirements.html) `BlockingIoCall` annotation.

### Changed
- `laboratory-android` artifact is now `laboratory-shared-preferences artifact`.
- `laboratory-shared-preferences` artifact (old `laboratory-android)` is no longer automatically applied by Gradle plugin in Android modules.
- Upgrade to Kotlin `1.4.10`.
- Upgrade to CoreKtx `1.3.2`.
- Upgrade to Wire `3.4.0`.
- Upgrade to KotlinPoet `1.6.0`.

## [0.2.0] - 2020-09-05
### Added
- `Laboratory.observe()` function to observe feature flag changes via [`Flow`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/).
- Support for [`DataStore`](https://developer.android.com/topic/libraries/architecture/datastore) with the `laboratory-date-store` artifact.
- `Laboratory` and `FeatureStorage` returns a boolean information whether writes are successful.
- `Feature` interface that is used to define feature flags.
- Wire `3.2.2` dependency to `laboratory-data-store`.

### Changed
- Kotlin standard library is now part of the public API.
- `Laboratory` and `FeatureStorage` expose their API via `suspend` functions.
- Gradle plugin requires exactly one feature flag value to be added with `withFallbackValue("Name")` function.
- Upgrade to Kotlin `1.4.0`.
- Upgrade to Material `1.2.1`.
- Upgrade to Hyperion `0.9.29`.


### Removed
- `@Feature` annotation. Feature flags should implement the `Feature` interface.

## [0.1.0] - 2020-08-03

- Initial release.

[Unreleased]: https://github.com/MiSikora/Laboratory/compare/0.8.0...HEAD
[0.8.0]: https://github.com/MiSikora/Laboratory/releases/tag/0.8.0
[0.7.0]: https://github.com/MiSikora/Laboratory/releases/tag/0.7.0
[0.6.2]: https://github.com/MiSikora/Laboratory/releases/tag/0.6.2
[0.6.1]: https://github.com/MiSikora/Laboratory/releases/tag/0.6.1
[0.6.0]: https://github.com/MiSikora/Laboratory/releases/tag/0.6.0
[0.5.0]: https://github.com/MiSikora/Laboratory/releases/tag/0.5.0
[0.4.0]: https://github.com/MiSikora/Laboratory/releases/tag/0.4.0
[0.3.0]: https://github.com/MiSikora/Laboratory/releases/tag/0.3.0
[0.2.1]: https://github.com/MiSikora/Laboratory/releases/tag/0.2.1
[0.2.0]: https://github.com/MiSikora/Laboratory/releases/tag/0.2.0
[0.1.0]: https://github.com/MiSikora/Laboratory/releases/tag/0.1.0
