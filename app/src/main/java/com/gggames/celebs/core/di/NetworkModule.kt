package com.gggames.celebs.core.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides

@Module
class NetworkModule {

    @Provides
    fun provideFirestore() = FirebaseFirestore.getInstance()

}