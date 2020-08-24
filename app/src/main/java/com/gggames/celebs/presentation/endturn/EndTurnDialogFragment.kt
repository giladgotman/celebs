package com.gggames.celebs.presentation.endturn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gggames.celebs.R
import com.gggames.celebs.features.video.VideoPlayer
import com.gggames.celebs.model.Card
import com.gggames.celebs.presentation.common.MainActivityDelegate
import com.gggames.celebs.presentation.di.createViewComponent
import kotlinx.android.synthetic.main.fragment_end_turn_dialog.*
import timber.log.Timber
import javax.inject.Inject

class EndTurnDialogFragment : Fragment(), MainActivityDelegate {

    private lateinit var cardsFoundAdapter: CardsFoundAdapter

    private var roundNumber: Int = 1

    @Inject
    lateinit var videoPlayer: VideoPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_end_turn_dialog, container, false)


    var finishPressed = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createViewComponent(this).inject(this)

        finishButton.setOnClickListener {
            finishPressed = true
            requireActivity().onBackPressed()
        }
        videoPlayer.initializePlayer()
        cardsFoundAdapter = CardsFoundAdapter { card, playerView, giftText ->

            val url = when (roundNumber) {
                1 -> { card.videoUrl1 }
                2 -> { card.videoUrl2 }
                3 -> { card.videoUrl3 }
                else -> { null }
            }
            url?.let {

                if (it.startsWith("text:")) {
                    if (giftText.tag != "open") {
                        giftText.text = it.removePrefix("text:")
                        giftText.tag = "open"
                    } else {
                        giftText.text = card.name
                        giftText.tag = null
                    }
                } else {
                    videoPlayer.setView(playerView)
                    playerView.isVisible = true
                    videoPlayer.playVideo(it)
                }
            }
        }

        cardsRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this.context)
        cardsRecyclerView.layoutManager = layoutManager
        cardsRecyclerView.itemAnimator = DefaultItemAnimator()
        cardsRecyclerView.adapter = cardsFoundAdapter

        arguments?.let {
            val name = it.getString(KEY_PLAYER_NAME) ?: ""
            roundNumber = it.getInt(KEY_ROUND_NUMBER)
            val cardsNames: Array<Card>? =
                it.getParcelableArray(KEY_CARDS) as Array<Card>

            val cardsList = cardsNames?.toList() ?: emptyList()
            cardsFoundAdapter.setData(cardsList)
            title.text = getString(R.string.end_turn_title, name)
            cardsAmountDescription.text =
                getString(R.string.end_turn_cards_description, cardsNames?.size ?: 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.w("onDestroyView videoPlayer: $videoPlayer")
        videoPlayer.releasePlayer()
    }

    override fun onBackPressed(): Boolean {
        // finish only if finish pressed
       return !finishPressed
    }
}

const val KEY_PLAYER_NAME = "playerName"
const val KEY_ROUND_NUMBER = "roundNumber"
const val KEY_CARDS = "cards"