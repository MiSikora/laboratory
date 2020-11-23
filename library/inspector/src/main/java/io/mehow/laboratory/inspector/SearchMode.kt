package io.mehow.laboratory.inspector

internal enum class SearchMode {
  Idle,
  Active,
  ;

  fun toggle() = when (this) {
    Idle -> Active
    Active -> Idle
  }
}
