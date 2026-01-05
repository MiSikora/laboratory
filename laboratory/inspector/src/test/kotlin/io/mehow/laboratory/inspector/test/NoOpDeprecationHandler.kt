package io.mehow.laboratory.inspector.test

import io.mehow.laboratory.inspector.DeprecationHandler
import io.mehow.laboratory.inspector.FeatureAlignment
import io.mehow.laboratory.inspector.FeatureStyle

internal val NoOpDeprecationHandler =
  DeprecationHandler(
    styleSelector = { FeatureStyle.Show },
    alignmentSelector = { FeatureAlignment.Regular },
  )
