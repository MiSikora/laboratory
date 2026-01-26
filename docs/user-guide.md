# User guide

## Features

Feature flags are defined as enum classes that implement `Feature` interface (or `BinaryFeature` for on/off flags). Each enum value is a possible option. You must provide a default option:

```kotlin
enum class MyFeature : Feature<MyFeature> {
  OptionA,
  OptionB,
  OptionC,
  ;

  override val defaultOption get() = OptionA
}

enum class MyBinaryFeature(
  override val binaryValue: Boolean,
) : BinaryFeature<MyBinaryFeature> {
  Enabled(true),
  Disabled(false),
  ;

  override val defaultOption get() = Enabled
}
```

!!! danger
    `Feature` enums must have at least one option. Defining an enum like below will make `Laboratory` throw an exception when used to read an option.

```kotlin
enum class MyFeature : Feature<MyFeature>
```

!!! tip
    Check [the samples](https://github.com/MiSikora/laboratory/tree/trunk/samples) to learn by example.

## Usage

Once you have a `Laboratory` instance, you can use its API to read and modify flags:

- `laboratory.experiment<FeatureType>()` to get the current option or `laboratory.observe<FeatureType>` to observe its value via `Flow`. If the flag has a remote source enabled, Laboratory will fetch from the appropriate storage based on the current source selection.
- `laboratory.experimentIs(featureOption)` to quickly check if the feature flag is currently set to use `featureOption`.
- `laboratory.isEnabled<BinaryFeatureType>` or `laboratory.observeBinary<BinaryFeatureType>` for convenient interactions with binary feature flags.
- `laboratory.setOption(featureOption)` to change the current option. This write only to the local source.
- `laboratory.localStorage().setLocalSource<FeatureType>()` or `laboratory.localStorage().setRemoteSource<FeatureType>()` to change the data source used for the feature flag when interacting with a `Laboratory` instance.

```kotlin
// Assume these are defined elsewhere
enum class Theme : Feature<Theme> { 
  Light,
  Dark,
  Contrast,
  ;
  
  override val defaultOption get() = Light
}

enum class ShowAds(
  override val binaryValue: Boolean,
) : BinaryFeature<ShowAds> { 
  Enabled(true),
  Disabled(false),
  ;
  
  override val defaultOption get() = Disabled
}

// Create the laboratory with in-memory storage
val laboratory = Laboratory.inMemory()

// Get current theme
val currentTheme = laboratory.experiment<Theme>()

// Toggle the theme
laboratory.setOption(Theme.Dark)

// Check if ads are enabled (binary flag)
val adsEnabled = laboratory.isEnabled<ShowAds>()
```

## Storage

By default all feature flags are configured to read their values from a local source. This can be changed overriding `Feature.source` value.

```kotlin
enum class MyFeature : Feature<MyFeature> {
  OptionA,
  OptionB,
  OptionC,
  ;

  override val defaultOption get() = OptionA

  override val defaultSource get() = Feature.Source.Remote
}
```

Laboratory uses `Storage` interface for persistence. The library provides built-in Storage implementations (in-memory, `SharedPreferences`, `DataStore`). Configure Laboratory by supplying a local and optionally a remote storage. If `remoteStorage` is omitted, Laboratory operates with local storage only.

```kotlin
val laboratory = Laboratory.Builder()
    .localStorage(Storage.inMemory())
    .remoteStorage(Storage.inMemory())
    .build()
```

!!! warning
    Because the persistence mechanism relies on package names and enum names, you should be careful when refactoring feature flags already available on production. Changing these options may result in a perception of unsaved feature flags.

Because `Storage` is an interface that is meant to be used with I/O operations, it exposes only `suspend` functions. `Laboratory`, on the other hand, allows you to opt-into blocking equivalents of read and write functions. You can selectively do this by applying the `@BlockingIoCall` annotation or globally by adding a compiler flag.

```groovy
android {
  kotlinOptions {
    freeCompilerArgs += [
        "-Xopt-in=io.mehow.laboratory.BlockingIoCall",
    ]
  }
}
```

Feature flags can be synced with a remote source with a help of `OptionFactory`. While it can be hand-written typically feature flags and option factory are generated using the [Gradle plugin](gradle-plugin.md). Below is a sample setup using Firebase.

```kotlin
enum class ChristmasTheme : Feature<ChristmasTheme> {
  Santa,
  Elves,
  Disabled,
  ;

  override val defaultOption get() = Disabled

  override val defaultSource get() = Feature.Source.Remote
}

enum class ShowAds : Feature<ShowAds> {
  Enabled,
  Disabled;

  override val defaultOption get() = Disabled

  override val defaultSource get() = Feature.Source.Remote
}

object CustomOptionFactory : OptionFactory {
  private val optionMapping = mapOf<String, (String) -> Feature<*>?>(
    "ChristmasTheme" to { name -> ChristmasTheme::class.java.enumConstants!!.firstOrNull { it.name == name } },
    "ShowAds" to { name -> ShowAds::class.java.enumConstants!!.firstOrNull { it.name == name } },
  )

  override fun create(key: String, name: String) = optionMapping[key]?.invoke(name)
}

class App : Application {
  override fun onCreate() {
    val laboratory = Laboratory.Builder()
        .localStorage(Storage.inMemory())
        .remoteStorage(Storage.inMemory())
        .build()

    // Get a reference to a node where feature flags are kept.
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

        // Use a coroutine scope that is appropriate for you application.
        GlobalScope.launch { laboratory.remoteStorage()?.setOptions(newOptions) }
      }

      override fun onCancelled(error: DatabaseError) = Unit
    }

    database.child("featureFlags").addValueEventListener(featureFlagListener)
  }
}
```