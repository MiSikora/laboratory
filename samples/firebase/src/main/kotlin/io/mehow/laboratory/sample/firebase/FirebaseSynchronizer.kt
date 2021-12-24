package io.mehow.laboratory.sample.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.OptionFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class FirebaseSynchronizer(
  private val databaseReference: DatabaseReference,
  private val optionFactory: OptionFactory,
  private val featureStorage: FeatureStorage,
) {
  fun synchronize(scope: CoroutineScope) = databaseReference.asKeyValueFlow()
      .map { pairs -> pairs.mapNotNull { (key, value) -> optionFactory.create(key, value) } }
      .onEach { featureStorage.setOptions(*it.toTypedArray()) }
      .launchIn(scope)

  private fun DatabaseReference.asKeyValueFlow() = callbackFlow {
    val listener = object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        (snapshot.value as? Map<*, *>)?.mapNotNull { (key, value) ->
          val stringKey = key as? String ?: return@mapNotNull null
          val stringValue = value as? String ?: return@mapNotNull null
          stringKey to stringValue
        }?.let { trySend(it) }
      }

      override fun onCancelled(error: DatabaseError) {
        close(error.toException())
      }
    }
    addValueEventListener(listener)

    awaitClose {
      removeEventListener(listener)
    }
  }
}
