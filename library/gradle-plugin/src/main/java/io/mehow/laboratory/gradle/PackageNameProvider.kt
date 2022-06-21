package io.mehow.laboratory.gradle

internal class PackageNameProvider : () -> String {
  var value = ""

  override fun invoke() = value
}
