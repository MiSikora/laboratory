package io.mehow.laboratory.inspector

import io.kotest.core.TestConfiguration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

internal fun TestConfiguration.setMainDispatcher(
  dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
  beforeSpec { Dispatchers.setMain(dispatcher) }
  afterSpec { Dispatchers.resetMain() }
}