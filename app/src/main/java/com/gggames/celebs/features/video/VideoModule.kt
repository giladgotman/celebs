package com.gggames.celebs.features.video

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class VideoModule {

    @Provides
    @Singleton
    fun provideVideoPlayer(player: ExoVideoPlayer): VideoPlayer = player
}