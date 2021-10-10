package io.mehow.laboratory

internal enum class FirstFeature : Feature<FirstFeature> {
  A,
  B,
  C,
  ;

  override val defaultOption get() = A

  override val source = Source::class.java

  enum class Source : Feature<Source> {
    Local,
    RemoteA,
    ;

    override val defaultOption get() = Local
  }
}

internal enum class SecondFeature : Feature<SecondFeature> {
  A,
  B,
  C,
  ;

  override val defaultOption get() = A

  override val source = Source::class.java

  enum class Source : Feature<Source> {
    Local,
    RemoteA,
    RemoteB,
    ;

    override val defaultOption get() = RemoteB
  }
}

internal enum class EmptySourceFeature : Feature<EmptySourceFeature> {
  A,
  B,
  C,
  ;

  override val defaultOption get() = A

  override val source = Source::class.java

  internal enum class Source : Feature<Source>
}

internal enum class UnsourcedFeature : Feature<UnsourcedFeature> {
  A,
  B,
  C,
  ;

  override val defaultOption get() = A
}

internal enum class SomeFeature : Feature<SomeFeature> {
  A,
  B,
  C,
  ;

  override val defaultOption get() = B
}

internal enum class OtherFeature : Feature<OtherFeature> {
  A,
  B,
  C,
  ;

  override val defaultOption get() = A
}

internal enum class NoValuesFeature : Feature<NoValuesFeature>

internal enum class GrandParentFeature : Feature<GrandParentFeature> {
  A,
  B,
  ;

  override val defaultOption get() = A
}

internal enum class ParentFeature : Feature<ParentFeature> {
  A,
  B,
  ;

  override val defaultOption get() = A

  override val supervisorOption = GrandParentFeature.A
}

internal enum class FirstChildFeature : Feature<FirstChildFeature> {
  A,
  B,
  ;

  override val defaultOption get() = A

  override val supervisorOption = ParentFeature.A
}

internal enum class SecondChildFeature : Feature<SecondChildFeature> {
  A,
  B,
  ;

  override val defaultOption get() = A

  override val supervisorOption = ParentFeature.B
}
