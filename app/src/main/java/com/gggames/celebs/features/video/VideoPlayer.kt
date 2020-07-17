package com.gggames.celebs.features.video

import android.content.Context
import android.graphics.Color
import android.net.Uri
import com.gggames.celebs.core.di.AppContext
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import javax.inject.Inject

interface Player {
    fun initializePlayer(playerView: PlayerView)
    fun releasePlayer()

}
class VideoPlayer @Inject constructor(@AppContext val context: Context) {

    private lateinit var player: SimpleExoPlayer
    private var mediaDataSourceFactory: DefaultDataSourceFactory? = null
    private var playWhenReady = true

    fun initializePlayer(playerView: PlayerView) {

        player = ExoPlayerFactory.newSimpleInstance(context)

        mediaDataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "mediaPlayerSample"))

        val url = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(
            Uri.parse(url))

        player.prepare(mediaSource, false, false)
        player.playWhenReady = playWhenReady

        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        playerView.player = player
        playerView.requestFocus()
    }

    fun releasePlayer() {
        player.release()
    }
}