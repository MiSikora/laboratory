# User guide

## Features

Feature flags are nothing more than enums that implement the `Feature` interface. It allows to define default value, remote sources that can provide different values, and some human-readable metadata.

!!! danger
    `Feature` enums must have at least one value. Defining an enum like below will make `Laboratory` throw an exception when used to read a value.

```kotlin
enum class SomeFeature : Feature<SomeFeature>
```

## I/O

`Laboratory` is nothing more than a high-level API over the `FeatureStorage` interface that is responsible for persisting feature flags. All implementations that are provided by this library rely on a feature flag package name and on an enum name.

!!! warning
    Because persistence mechanism relies on package names and enum names you should be careful when refactoring feature flags that are already available on production. Changing these values may result in a perception of unsaved feature flags.

Because `FeatureStorage` is an interface that is meant to be used with I/O operations it exposes only `suspend` functions. `Laboratory`, on the other hand, allows you to opt-into blocking equivalents of read and write functions. You can do this either selectively by applying the `@BlockingIoCall` annotation, or globally by adding a compiler flag.

```groovy
android {
  kotlinOptions {
    freeCompilerArgs += "-Xopt-in=io.mehow.laboratory.BlockingIoCall"
  }
}
```

In either case a design that relies on non-blocking function calls is preferable.

## Sources

Feature flags by default have only a single source for their values. By convention it is considered to be a local source. However, you might have a need to have different data sources for feature flags depending on some runtime conditions or depending on a build variant. For example, you might want to use a local source during debugging and rely on some remote service in production.

Let's say that you want to have a feature flag that has three sources. One local, and two remote ones.

!!! info
    Notice that a feature flag source is also a feature flag. This allows to change feature flag sources via `Laboratory` as well.

```kotlin
enum class PowerType : Feature<PowerType> {
  Coal,
  Wind,
  Solar;

  public override val defaultOption get() = Solar

  @Suppress("UNCHECKED_CAST")
  override val source = Source::class.java as Class<Feature<*>>

  enum class Source : Feature<Source> {
    Local,
    Firebase,
    Azure;

    public override val defaultOption get() = Firebase
  }
}
```

If you define multiple sources for a feature flag you should add a `Local` enum value to them. This allows to change feature flag values at runtime from the [QA module](qa-module.md).

This feature flag definition allows to configure `Laboratory` in a way that it is capable of recognizing that `PowerType` has different value providers, and that default provider is `Firebase`.

Because the `Laboratory` only delegates its work to `FeatureStorage`, it is `FeatureStorage` that needs to understand how to connect feature flags with their sources. This is possible with a special implementation of this interface that is available as an extension function.

```kotlin
val sourcedFeatureStorage = FeatureStorage.sourced(
  localSource = FeatureStorage.inMemory(),
  remoteSources = mapOf(
    "Firebase" to FeatureStorage.inMemory(),
    "Azure" to FeatureStorage.inMemory(),
  ),
)
```

`sourcedFeatureStorage` delegates persistence mechanism to three different storage and is responsible for coordination of a selected source and a current feature flag value.

One thing that is error-prone is the fact that `sourcedFeatureStorage` relies on strings and source names to use the correct storage. The reason for this is that two different feature flags might share sources partially.

!!! tip
    Using [Gradle plugin](gradle-plugin.md) allows you to avoid this issue with generation of a custom `FeatureStorage` that is always up-to-date.

```kotlin
enum class PowerType : Feature<PowerType> {
  Coal,
  Wind,
  Solar;

  public override val defaultOption get() = Solar

  @Suppress("UNCHECKED_CAST")
  override val source = Source::class.java as Class<Feature<*>>

  enum class Source : Feature<Source> {
    Local,
    Firebase,
    Azure;

    public override val defaultOption get() = Firebase
  }
}

enum class Theme : Feature<PowerType> {
  Night,
  Day,
  Christmas;

  public override val defaultOption get() = Night

  @Suppress("UNCHECKED_CAST")
  override val source = Source::class.java as Class<Feature<*>>

  enum class Source : Feature<Source> {
    Local,
    Azure;

    public override val defaultOption get() = Local
  }
}
```

In this case `Theme` and `PowerType` feature flags share `Azure` source but `Firebase` is applicable only to the `PowerType` flag.

```kotlin
// Create laboratory that understands sourced features
val laboratory = Laboratory(sourcedFeatureStorage)

// Check value of PowerType in Firebase FeatureStorage
val powerTypeFirebaseValue = laboratory.experiment<PowerType>()

// Check value of Theme in local FeatureStorage
val themeLocalValue = laboratory.experiment<Theme>()

// Set source of Theme source to Azure (PowerType is still unaffected and uses Firebase)
val success = laboratory.setFeature(Theme.Source.Azure)

// Check value of Theme in Azure FeatureStorage
val themeAzureValue laboratory.experiment<Theme>()
```

!!! info
    The implementation of `sourcedFeatureStorage` provided by the library saves data only in `localSource`.

In order to propagate remote feature flag values on updates they need to be connected to a remote source.

```kotlin
enum class ShowAds : Feature<ShowAds> {
  Enabled,
  Disabled;

  public override val defaultOption get() = Disabled

  @Suppress("UNCHECKED_CAST")
  override val source = Source::class.java as Class<Feature<*>>

  enum class Source : Feature<Source> {
    Local,
    Remote;

    public override val defaultOption get() = Remote
  }
}

val firebaseStorage = FeatureStorage.inMemory()
val sourcedFeatureStorage = FeatureStorage.sourced(
  localSource = FeatureStorage.inMemory(),
  remoteSources = mapOf("Remote" to firebaseStorage),
)

// During application initialisation
val laboratory = Laboratory(firebaseStorage)
remoteService.observeShowAdsFlag()
    // Some custom mapping between a service value and a feature flag
    .map { showAds: Boolean ->
      val showAdsFlag = if (showAds) ShowAds.Enabled else ShowAds.Disabled
      laboratory.setFeature(showAdsFlag)
    }
    // Scope should last for the lifetime of an application
    .launchIn(GlobalScope)
```
