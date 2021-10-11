package io.mehow.laboratory.sample.firebase

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.OptionFactory
import io.mehow.laboratory.datastore.FeatureFlagsSerializer
import io.mehow.laboratory.datastore.dataStore
import io.mehow.laboratory.inspector.LaboratoryActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import java.io.File
import android.app.Application as AndroidApplication

class Application : AndroidApplication() {
  private lateinit var laboratory: Laboratory

  override fun onCreate() {
    super.onCreate()
    val localDataStore = DataStoreFactory.create(FeatureFlagsSerializer) { File(filesDir, "datastore/local") }
    val localStorage = FeatureStorage.dataStore(localDataStore)
    val firebaseDataStore = DataStoreFactory.create(FeatureFlagsSerializer) { File(filesDir, "datastore/firebase") }
    val firebaseStorage = FeatureStorage.dataStore(firebaseDataStore)
    val sourcedStorage = FeatureStorage.sourcedBuilder(localStorage)
        .firebaseSource(firebaseStorage)
        .build()
    laboratory = Laboratory.create(sourcedStorage)
    LaboratoryActivity.configure(laboratory, FeatureFactory.featureGenerated())

    val synchronizer = FirebaseSynchronizer(
        databaseReference = Firebase.database(firebaseUrl).reference.child("featureFlags"),
        optionFactory = OptionFactory.generated(),
        featureStorage = firebaseStorage,
    )
    @OptIn(DelicateCoroutinesApi::class) synchronizer.synchronize(GlobalScope)
  }

  companion object {
    private const val firebaseUrl = "https://laboratory-sample-default-rtdb.europe-west1.firebasedatabase.app"

    val Context.laboratory get() = (applicationContext as Application).laboratory
  }
}
