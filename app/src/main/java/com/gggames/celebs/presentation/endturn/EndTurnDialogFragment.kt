package com.gggames.celebs.presentation.endturn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gggames.celebs.R
import com.gggames.celebs.model.Card
import kotlinx.android.synthetic.main.fragment_end_turn_dialog.*
import timber.log.Timber

class EndTurnDialogFragment : Fragment() {

    private lateinit var cardsFoundAdapter: CardsFoundAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_end_turn_dialog, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardsFoundAdapter = CardsFoundAdapter {
            Timber.w("ggg click url: ${it.videoUrl1}")
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
            cardsFoundAdapter.setData(cardsList)
            title.text = getString(R.string.end_turn_title, name)
            cardsAmountDescription.text =
                getString(R.string.end_turn_cards_description, cardsNames?.size ?: 0)
        }
    }

}

const val KEY_PLAYER_NAME = "KEY_PLAYER_NAME"
const val KEY_CARDS = "cards"