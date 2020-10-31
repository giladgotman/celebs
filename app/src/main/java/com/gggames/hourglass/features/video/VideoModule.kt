package com.gggames.hourglass.features.video

import android.content.Context
import com.gggames.hourglass.core.di.AppContext
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.Module
import dagger.Provides

@Module
class VideoModule {

    @Provides
    fun provideVideoPlayer(player: ExoVideoPlayer): VideoPlayer = player

    @Provides
    fun provideExoSimplePlayer(@AppContext context: Context): SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context)
}
