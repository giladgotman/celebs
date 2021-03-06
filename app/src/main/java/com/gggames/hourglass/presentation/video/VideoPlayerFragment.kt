package com.gggames.hourglass.presentation.video

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gggames.hourglass.R
import com.gggames.hourglass.features.video.VideoPlayer
import com.gggames.hourglass.presentation.di.createViewComponent
import kotlinx.android.synthetic.main.fragment_video_player.*
import javax.inject.Inject

class VideoPlayerFragment : Fragment(){

    @Inject
    lateinit var videoPlayer: VideoPlayer

    lateinit var videoPlayerPresenter: VideoPlayerPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createViewComponent(this).inject(this)

        videoPlayer.initializePlayer()
        videoPlayer.setView(playerView)

        arguments?.let {
            it.getString(VIDEO_URL_KEY)?.let {url->
                playVideo(url)
            }
        }
    }

    private fun playVideo(url: String) {
        videoPlayer.playVideo(url)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        videoPlayer.releasePlayer()
    }
}

const val VIDEO_URL_KEY = "videoUrl"