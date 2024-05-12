package io.mehow.laboratory.inspector

import androidx.fragment.app.Fragment

internal fun Fragment.requireStringArgument(key: String) = requireNotNull(requireArguments().getString(key)) {
  "Missing key: $key"
}
