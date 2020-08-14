package com.gggames.celebs.features.video

import android.content.Context
import android.graphics.Color
import android.media.session.PlaybackState.STATE_BUFFERING
import android.net.Uri
import android.widget.Toast
import androidx.core.view.isVisible
import com.gggames.celebs.core.di.AppContext
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.PlayerView.SHOW_BUFFERING_WHEN_PLAYING
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

interface VideoPlayer {
    fun initializePlayer()
    fun setView(playerView: PlayerView)
    fun releasePlayer()
    fun playVideo(url: String)
    val events: Observable<PlayerEvent>
}

class ExoVideoPlayer @Inject constructor(
    @AppContext val context: Context,
    val player: SimpleExoPlayer
) : VideoPlayer {

    private var mediaDataSourceFactory: DefaultDataSourceFactory? = null

    private var _playerView: PlayerView? = null

    override val events = PublishSubject.create<PlayerEvent>()

    override fun initializePlayer() {

        mediaDataSourceFactory =
            DefaultDataSourceFactory(context, Util.getUserAgent(context, "mediaPlayerSample"))

        player.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
                Timber.d("onPlaybackParametersChanged: ")
            }

            override fun onTracksChanged(
                trackGroups: TrackGroupArray?,
                trackSelections: TrackSelectionArray?
            ) {
                Timber.d("onTracksChanged: ")
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                Timber.e("onPlayerError: ${error.toString()}")
                events.onNext(PlayerEvent.OnError(error))
                _playerView?.isVisible = false
                Toast.makeText(context, "Error playing video", Toast.LENGTH_LONG).show()
            }

            /** 4 playbackState exists */
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                Timber.d("onPlayerStateChanged: state: $playbackState")
                when (playbackState) {
                    STATE_BUFFERING -> {
                        events.onNext(PlayerEvent.OnBufferingState)
                        _playerView?.isVisible = true
                    }
                    STATE_READY -> {
                        events.onNext(PlayerEvent.OnReadyState)
                        _playerView?.isVisible = true
                    }
                    STATE_IDLE -> {
                        events.onNext(PlayerEvent.OnIdleState)
                    }
                    STATE_ENDED -> {
                        events.onNext(PlayerEvent.OnEndedState)
                        _playerView?.isVisible = false
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
    }

    override fun setView(playerView: PlayerView) {
        _playerView?.player = null
        _playerView = playerView
        setupPlayerView()
    }

    private fun setupPlayerView() {
        _playerView?.useController = false
        _playerView?.setShowBuffering(SHOW_BUFFERING_WHEN_PLAYING)
        _playerView?.resizeMode = RESIZE_MODE_FIXED_HEIGHT
        _playerView?.setOnClickListener {
            _playerView?.useController = true
        }

        _playerView?.setShutterBackgroundColor(Color.TRANSPARENT)
        _playerView?.player = player
        _playerView?.requestFocus()
    }

    override fun playVideo(url: String) {
        Timber.d("playVideo: $url")
        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(
            Uri.parse(url)
        )

        player.prepare(mediaSource)
        player.playWhenReady = true
    }

    override fun releasePlayer() {
        _playerView?.player = null
        player.release()
    }
}

sealed class PlayerEvent {
    object OnIdleState : PlayerEvent()
    object OnReadyState : PlayerEvent()
    object OnBufferingState : PlayerEvent()
    object OnEndedState : PlayerEvent()
    data class OnError(val error: ExoPlaybackException?) : PlayerEvent()
}
