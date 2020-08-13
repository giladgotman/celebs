package com.gggames.celebs.presentation.endturn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gggames.celebs.R
import com.gggames.celebs.features.video.VideoPlayer
import com.gggames.celebs.model.Card
import com.gggames.celebs.presentation.di.createViewComponent
import kotlinx.android.synthetic.main.fragment_end_turn_dialog.*
import javax.inject.Inject

class EndTurnDialogFragment : Fragment() {

    private lateinit var cardsFoundAdapter: CardsFoundAdapter

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

        finishButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        videoPlayer.initializePlayer()

        cardsFoundAdapter = CardsFoundAdapter {card, playerView->
            val url = card.videoUrl1
            url?.let {
                videoPlayer.setView(playerView)
                videoPlayer.playVideo(it)
            }
        }

        cardsRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this.context)
        cardsRecyclerView.layoutManager = layoutManager
        cardsRecyclerView.itemAnimator = DefaultItemAnimator()
        cardsRecyclerView.adapter = cardsFoundAdapter

        arguments?.let {
            val name = it.getString(KEY_PLAYER_NAME) ?: ""
            val cardsNames: Array<Card>? =
                it.getParcelableArray(KEY_CARDS) as Array<Card>

            val cardsList = cardsNames?.toList() ?: emptyList()
            // FAKE
            val fakeList = cardsList.toMutableList()
            fakeList.addAll(cardsList)
            fakeList.addAll(cardsList)
            fakeList.addAll(cardsList)
            fakeList.addAll(cardsList)
            fakeList.addAll(cardsList)
//            cardsFoundAdapter.setData(fakeList)
            // ----- FAKE
            cardsFoundAdapter.setData(cardsList)
            title.text = getString(R.string.end_turn_title, name)
            cardsAmountDescription.text =
                getString(R.string.end_turn_cards_description, cardsNames?.size ?: 0)
        }
    }

}

const val KEY_PLAYER_NAME = "KEY_PLAYER_NAME"
const val KEY_CARDS = "cards"