package io.mehow.laboratory.gradle

import java.io.Serializable

internal class PackageNameProvider(
  private val delegate: PackageNameProvider? = null,
) : Serializable {
  var value: String? = null
    get() = field ?: delegate?.value
    private set

  fun setValue(value: String?) {
    this.value = value
  }

  internal companion object {
    private const val serialVersionUID = 0L
  }
}
