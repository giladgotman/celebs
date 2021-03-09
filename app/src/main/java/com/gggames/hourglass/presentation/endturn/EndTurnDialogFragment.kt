package com.gggames.hourglass.presentation.endturn

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.gggames.hourglass.R
import com.gggames.hourglass.features.video.VideoPlayer
import com.gggames.hourglass.model.Card
import com.gggames.hourglass.model.Player
import com.gggames.hourglass.model.PlayerTurnState
import com.gggames.hourglass.presentation.common.NameBadge
import com.gggames.hourglass.presentation.di.createViewComponent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_end_turn_dialog.*
import timber.log.Timber
import javax.inject.Inject

class EndTurnDialogFragment : BottomSheetDialogFragment() {

    private lateinit var cardsFoundAdapter: CardsFoundAdapter

    private var roundNumber: Int = 1


    fun show(activity: AppCompatActivity) {
        show(activity.supportFragmentManager, this.javaClass.simpleName)
    }

    @Inject
    lateinit var videoPlayer: VideoPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_end_turn_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createViewComponent(this).inject(this)

        closeButton.setOnClickListener {
            Timber.w("close click: dialog:$dialog")
            dismiss()
        }
        videoPlayer.initializePlayer()
        cardsFoundAdapter = CardsFoundAdapter (onClick = { card, playerView, giftText ->

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
        }, onClose = {playerView->
            playerView.isVisible = false
            videoPlayer.stop()
        })

        cardsRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this.context, HORIZONTAL, false)
        cardsRecyclerView.layoutManager = layoutManager
        cardsRecyclerView.itemAnimator = DefaultItemAnimator()
        cardsRecyclerView.adapter = cardsFoundAdapter

        arguments?.let {
            val name = it.getString(KEY_PLAYER_NAME) ?: ""
            val nextPlayerName = it.getString(KEY_NEXT_PLAYER_NAME ) ?: ""
            roundNumber = it.getInt(KEY_ROUND_NUMBER)
            val cardsNames: Array<Card>? =
                it.getParcelableArray(KEY_CARDS) as Array<Card>

            val cardsList = cardsNames?.toList() ?: emptyList()
            cardsFoundAdapter.setData(cardsList)
            title.text = getString(R.string.end_turn_title, name)
            if (nextPlayerName.isNotEmpty()) {
                nextPlayerNameBadge.state =
                    NameBadge.State(nextPlayerName, PlayerTurnState.UpNext)
            } else {
                nextPlayerLabel.isVisible = false
                nextPlayerNameBadge.isVisible = false
            }
            cardsAmountDescription.text =
                getString(R.string.end_turn_cards_description, cardsNames?.size ?: 0)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.w("onDestroyView videoPlayer: $videoPlayer")
        videoPlayer.releasePlayer()
    }

    companion object {
        fun create(player: Player, nextPlayer: Player?, cards: List<Card>, roundNumber: Int): EndTurnDialogFragment {
            return EndTurnDialogFragment()
                .apply {
                    isCancelable = true
                    arguments =
                        Bundle().apply {
                            putString(KEY_PLAYER_NAME, player.name)
                            nextPlayer?.let {
                                putString(KEY_NEXT_PLAYER_NAME, nextPlayer.name)
                            }
                            putParcelableArray(KEY_CARDS, cards.toTypedArray())
                            putInt(KEY_ROUND_NUMBER, roundNumber)
                        }
                }
        }
        const val KEY_PLAYER_NAME = "playerName"
        const val KEY_NEXT_PLAYER_NAME = "nextPlayerName"
        const val KEY_ROUND_NUMBER = "roundNumber"
        const val KEY_CARDS = "cards"
    }
}

