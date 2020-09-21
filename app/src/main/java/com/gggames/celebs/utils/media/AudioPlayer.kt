package com.gggames.celebs.utils.media

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.gggames.celebs.BuildConfig
import com.gggames.celebs.core.di.AppContext
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class AudioPlayer @Inject constructor(
    @AppContext private val appContext: Context

) {
    private var mediaPlayer: MediaPlayer? = null

    fun play(rawAudioFileName: String) {
        play(Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/raw/$rawAudioFileName"))
    }

    fun play(uri: Uri) {
        try {
            mediaPlayer = MediaPlayer.create(appContext, uri)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                it.release()
            }
        } catch (e: Exception) {
            Timber.e(e, "play error")
            mediaPlayer?.release()
        }
    }

    fun release() {
        mediaPlayer?.release()
    }
}
