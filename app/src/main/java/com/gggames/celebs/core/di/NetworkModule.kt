package com.gggames.celebs.core.di

import com.gggames.celebs.common.BASE_FIRE_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class NetworkModule {

    @Provides
    fun provideFirestore() = FirebaseFirestore.getInstance()

    @Provides
    @Named("baseFirebaseCollection")
    fun provideFirebaseBaseCollection() = BASE_FIRE_COLLECTION

}