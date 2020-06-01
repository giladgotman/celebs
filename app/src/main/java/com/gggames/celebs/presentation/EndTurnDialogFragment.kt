package com.gggames.celebs.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.gggames.celebs.R
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Player
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_end_turn_dialog.*

class EndTurnDialogFragment
    : BottomSheetDialogFragment() {

    fun show(activity: AppCompatActivity) {
        show(activity.supportFragmentManager, this.javaClass.simpleName)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_end_turn_dialog, container, false)

    private val KEY_PLAYER_NAME = "KEY_PLAYER_NAME"
    private val KEY_CARD_NAMES = "KEY_CARD_NAMES"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val name = it.getString(KEY_PLAYER_NAME) ?: ""
            val cards: ArrayList<String>? = it.getStringArrayList(KEY_CARD_NAMES) as ArrayList<String>

            title.text = getString(R.string.end_turn_title, name)
            cardsAmountDescription.text =
                getString(R.string.end_turn_cards_description, cards?.size ?: 0)
        }

        buttonDone.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun create(player: Player, cards: List<Card>): EndTurnDialogFragment {
            return EndTurnDialogFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(KEY_PLAYER_NAME, player.name)
                        putStringArrayList(KEY_CARD_NAMES, ArrayList(cards.map { it.name }))
                    }
            }
        }

    }
}
