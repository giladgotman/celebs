package com.gggames.celebs.features.video

import android.content.Context
import com.gggames.celebs.core.di.AppContext
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class VideoModule {

    @Provides
    @Singleton
    fun provideVideoPlayer(player: ExoVideoPlayer): VideoPlayer = player

    @Provides
    @Singleton
    fun provideExoSimplePlayer(@AppContext context: Context): SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context)
}
