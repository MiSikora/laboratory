Change Log
==========

Version 0.2.1 *(2020-10-02)*
----------------------------

* Enable to read and write features with Laboratory in an opt in, blocking way.
* Renamed `android` artifact to `shared-preferences` artifact.
* `shared-preferences` artifact (old `android`) is no longer applied by Gradle automatically.

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
