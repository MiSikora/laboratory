# User guide

## Features

Feature flags are nothing more than enums that implement the `Feature` interface. It allows us to define a default option, remote sources that can provide different options and descriptions for some human-readable metadata.

!!! danger
    `Feature` enums must have at least one option. Defining an enum like below will make `Laboratory` throw an exception when used to read an option.

```kotlin
enum class SomeFeature : Feature<SomeFeature>
```

!!! tip
    Check [the samples](https://github.com/MiSikora/laboratory/tree/trunk/samples) to learn by example.

## I/O

`Laboratory` is nothing more than a high-level API over the `FeatureStorage` interface responsible for persisting feature flags. All implementations that are provided by this library rely on a feature flag package name and an enum name.

!!! warning
    Because the persistence mechanism relies on package names and enum names, you should be careful when refactoring feature flags already available on production. Changing these options may result in a perception of unsaved feature flags.

Because `FeatureStorage` is an interface that is meant to be used with I/O operations, it exposes only `suspend` functions. `Laboratory`, on the other hand, allows you to opt-into blocking equivalents of read and write functions. You can selectively do this by applying the `@BlockingIoCall` annotation or globally by adding a compiler flag.

```groovy
android {
  kotlinOptions {
    freeCompilerArgs += "-Xopt-in=io.mehow.laboratory.BlockingIoCall"
  }
}
```

In either case, a design that relies on non-blocking function calls is preferable.

## Sources

Feature flags, by default, have only a single source for their options. By convention, it is considered to be a local source. However, you might need to have different data sources for feature flags, depending on some runtime conditions or a build variant. For example, you might want to use a local source during debugging and rely on some remote services on production.

Let's say that you want to have a feature flag that has three sources. One local, and two remote ones.

!!! info
    Notice that a feature flag source is also a feature flag. This allows us to change feature flag sources via `Laboratory` as well.

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

If you define multiple sources for a feature flag, you should add a `Local` option to them. This allows changing feature flag options at runtime from the [QA module](qa-module.md).

This feature flag definition allows configuring `Laboratory` in a way that it is capable of recognizing that `PowerType` has different option providers and that the default provider is `Firebase`.

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

`sourcedFeatureStorage` delegates persistence mechanism to three different storage and is responsible for coordinating a selected source and a current feature flag option.

One error-prone thing is that `sourcedFeatureStorage` relies on strings and source names to use the correct storage. The reason for this is that two different feature flags might share sources partially.

!!! tip
    Using [Gradle plugin](gradle-plugin.md) allows you to avoid this issue with the generation of a custom `FeatureStorage` that is always up-to-date.

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

In this case, `Theme` and `PowerType` feature flags share `Azure` source, but `Firebase` applies only to the `PowerType` flag.

```kotlin
// Create laboratory that understands sourced features
val laboratory = Laboratory.create(sourcedFeatureStorage)

// Check option of PowerType in Firebase FeatureStorage
val powerTypeFirebaseValue = laboratory.experiment<PowerType>()

// Check option of Theme in local FeatureStorage
val themeLocalValue = laboratory.experiment<Theme>()

// Set source of Theme source to Azure (PowerType is still unaffected and uses Firebase)
val success = laboratory.setOption(Theme.Source.Azure)

// Check option of Theme in Azure FeatureStorage
val themeAzureValue = laboratory.experiment<Theme>()
```

!!! info
    The implementation of `sourcedFeatureStorage` provided by the library saves data only in `localSource`.

To propagate remote feature flag options on updates, they need to be connected to a remote source.

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
val laboratory = Laboratory.create(sourcedFeatureStorage)
remoteService.observeShowAdsFlag()
    // Some custom mapping between a service option and a feature flag
    .map { showAds: Boolean ->
      val showAdsFlag = if (showAds) ShowAds.Enabled else ShowAds.Disabled
      laboratory.setOption(showAdsFlag)
    }
    // Scope should last for the lifetime of an application
    .launchIn(GlobalScope)
```

## Default options override

Whenever Laboratory reads an option for a feature flag, it falls back to a default option declared on a said flag. However, there might be cases when you'd like to change the default behavior. One example might be having features enabled by default in your debug builds and disabled on production. Or you might use feature flags for configuration, and you'd like to have a different configuration per build variant. Laboratory enables this with default options overrides.

```kotlin
enum class ShowAds : Feature<ShowAds> {
  Enabled,
  Disabled;

  public override val defaultOption get() = Disabled
}

object DebugDefaultOptionFactory : DefaultOptionFactory {
  override fun <T : Feature<T>> create(feature: T): Feature<*>? = when(feature) {
    is ShowAds -> ShowAds.Enabled
    else -> null
  }
}

val laboratory = Laboratory.builder()
    .featureStorage(FeatureStorage.inMemory())
    .defaultOptionFactory(DebugDefaultOptionFactory)
    .build()

// Uses default option declared in DebugDefaultOptionFactory
laboratory.experimentIs(ShowAds.Enabled)
```

You can be even more creative and, for example, enable all feature flags in your debug builds, which have an option `Enabled`.

```kotlin
class DebugDefaultOptionFactory : DefaultOptionFactory {
  override fun <T : Feature<T>> create(feature: T): Feature<*>? {
    return feature.options.associateBy { it.name }["Enabled"]
  }

  private val <T : Feature<T>> T.options get() = javaClass.options
}
```

## Feature flag supervision

Feature flags can be supervised using `FeatureFlag.supervisorOption` property. Whenever supervisor has its option different from the value in this property then the supervised feature flag cannot return any other option than a default one. Option can still be set via `Laboratory` but it will not be exposed as long as a feature flag is not supervised. This relationship is recursive meaning that grandparents control grandchildren indirectly.

```kotlin
enum class ChristmasTheme : Feature<ChristmasTheme> {
  Enabled,
  Disabled,
  ;

  public override val defaultOption get() = Disabled
}

enum class Greeting : Feature<Greeting> {
  Hello,
  HoHoHo,
  ;

  public override val defaultOption get() = Hello

  public override val supervisorOption get() = ChristmasTheme.Enabled
}

enum class Background : Feature<Background> {
  White,
  Reindeer,
  Snowman,
  ;

  public override val defaultOption get() = White

  public override val supervisorOption get() = ChristmasTheme.Enabled
}

val laboratory = Laboratory.inMemory()

laboratory.setOptions(Greeting.HoHoHo, Background.Reindeer)

laboratory.experimentIs(Greeting.HoHoHo) // false
laboratory.experimentIs(Background.Reindeer) // false

laboratory.setOption(ChristmasTheme.Enabled)

laboratory.experimentIs(Greeting.HoHoHo) // true
laboratory.experimentIs(Background.Reindeer) // true
```

## Listening to remote change

Feature flags can be synced with a remote source with a help of `OptionFactory`. Below is a sample setup using Firebase.

```kotlin
enum class ChristmasTheme : Feature<ChristmasTheme> {
  Enabled,
  Disabled,
  ;

  public val override val defaultOption get() = Disabled
}

enum class ShowAds : Feature<ShowAds> {
  Enabled,
  Disabled;

  public override val defaultOption get() = Disabled
}

object CustomOptionFactory : OptionFactory {
  private val optionMapping = mapOf<String, (String) -> Feature<*>?>(
    "ChristmasTheme" to { name -> ChristmasTheme::class.java.options.firstOrNull { it.name == name } },
    "ShowAds" to { name -> ShowAds::class.java.options.firstOrNull { it.name == name } },
  )

  override fun create(key: String, name: String) = optionMapping[key]?.invoke(name)
}

class App : Application {
  override fun onCreate() {
    val firebaseStorage = FeatureStorage.inMemory()
    // Get a reference to a node where feature flags are kept
    val database = FirebaseDatabase.getInstance().reference.child("featureFlags")

    val featureFlagListener = object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        val newOptions = (snapshot.value as? Map<*, *>)
            .orEmpty()
            .mapNotNull { (key, value) ->
              val stringKey = key as? String ?: return@mapNotNull null
              val stringValue = value as? String ?: return@mapNotNull null
              CustomOptionFactory.create(stringKey, stringValue)
            }
        // Be cautious with using GlobalScope.
        GlobalScope.launch { firebaseStorage.setOptions(*newOptions.toTypedArray()) }
      }

      override fun onCancelled(error: DatabaseError) = Unit
    }

    database.child("featureFlags").addValueEventListener(featureFlagListener)
  }
}
```
