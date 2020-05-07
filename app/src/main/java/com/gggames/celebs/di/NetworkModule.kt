package com.gggames.celebs.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides

@Module
class NetworkModule {

    @Provides
    fun provideFirestore() = FirebaseFirestore.getInstance()

}