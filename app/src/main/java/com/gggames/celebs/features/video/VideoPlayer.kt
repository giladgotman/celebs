package com.gggames.celebs.features.video

import android.content.Context
import android.graphics.Color
import android.media.session.PlaybackState.STATE_BUFFERING
import android.net.Uri
import androidx.core.view.isVisible
import com.gggames.celebs.core.di.AppContext
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.PlayerView.SHOW_BUFFERING_WHEN_PLAYING
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

interface VideoPlayer {
    fun initializePlayer(playerView: PlayerView)
    fun releasePlayer()
    fun playVideo(url: String)
    val events: Observable<PlayerEvent>
}

class ExoVideoPlayer @Inject constructor(
    @AppContext val context: Context,
    val player: SimpleExoPlayer
) : VideoPlayer {

    private var mediaDataSourceFactory: DefaultDataSourceFactory? = null

    override val events = PublishSubject.create<PlayerEvent>()

    override fun initializePlayer(playerView: PlayerView) {

        setupPlayerView(playerView)

        mediaDataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "mediaPlayerSample"))

        player.addListener( object : Player.EventListener{
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
                Timber.d( "onPlaybackParametersChanged: ")
            }

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                Timber.d("onTracksChanged: ")
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                Timber.d("onPlayerError: ")
                events.onNext(PlayerEvent.OnError(error))
            }

            /** 4 playbackState exists */
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                Timber.d("onPlayerStateChanged: state: $playbackState")
                when(playbackState){
                    STATE_BUFFERING -> {
                        events.onNext(PlayerEvent.OnBufferingState)
                        playerView.isVisible = true
                    }
                    STATE_READY -> {
                        events.onNext(PlayerEvent.OnReadyState)
                        playerView.isVisible = true
                    }
                    STATE_IDLE -> {
                        events.onNext(PlayerEvent.OnIdleState)
                    }
                    STATE_ENDED -> {
                        events.onNext(PlayerEvent.OnEndedState)
                        playerView.isVisible = false

                    }
                }
            }

            override fun onLoadingChanged(isLoading: Boolean) {
            }

            override fun onPositionDiscontinuity(reason: Int) {
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            }
        })

        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        playerView.player = player
        playerView.requestFocus()
    }

    private fun setupPlayerView(playerView: PlayerView) {
        playerView.useController = false
        playerView.setShowBuffering(SHOW_BUFFERING_WHEN_PLAYING)
        playerView.setOnClickListener {
            playerView.useController = true
        }
    }

    override fun playVideo(url: String) {
        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(
            Uri.parse(url))

        player.prepare(mediaSource)
        player.playWhenReady = true
    }

    override fun releasePlayer() {
        player.release()
    }
}

sealed class PlayerEvent {
    object OnIdleState: PlayerEvent()
    object OnReadyState: PlayerEvent()
    object OnBufferingState: PlayerEvent()
    object OnEndedState: PlayerEvent()
    data class OnError(val error: ExoPlaybackException?): PlayerEvent()
}
