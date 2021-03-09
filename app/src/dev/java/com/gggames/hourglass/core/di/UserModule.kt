package com.gggames.hourglass.core.di

import com.gggames.hourglass.features.user.data.UserDataSource
import com.gggames.hourglass.features.user.data.UserRepository
import com.gggames.hourglass.features.user.data.UserRepositoryImpl
import com.gggames.hourglass.features.user.data.remote.UserDataSourceFake
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UserModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        repository: UserRepositoryImpl
    ): UserRepository = repository

    @Provides
    @Singleton
    fun provideUserDataSource(
        dataSource: UserDataSourceFake
    ): UserDataSource = dataSource
}
