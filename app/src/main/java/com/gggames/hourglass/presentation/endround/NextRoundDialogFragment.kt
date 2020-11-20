package com.gggames.hourglass.presentation.endturn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.gggames.hourglass.R
import com.gggames.hourglass.model.Turn
import kotlinx.android.synthetic.main.fragment_next_round.*

class NextRoundDialogFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_next_round, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            val nextRoundId = bundle.getInt(KEY_NEXT_ROUND_ID)
            val nextRoundName = bundle.getString(KEY_NEXT_ROUND_NAME)
            val currentTurn = bundle.getParcelable<Turn?>(KEY_CURRENT_TURN)
            title.text = getString(R.string.next_round_title, nextRoundId, nextRoundName)

            next_round_description.text = getText(R.string.end_round_round2_description)
            if (currentTurn?.player != null) {
                val secondsLeft =  currentTurn.time?.let { (it / 1000).toInt() % 60 } ?: 0
                next_round_next_player_value.text = getString(
                    R.string.next_round_next_player_value,
                    currentTurn.player.name,
                    secondsLeft
                )
            } else {
                next_round_next_player_lbl.isVisible = false
            }
        }
    }

    companion object {
        fun createArgs(nextRoundId: Int, nextRoundName: String, currentTurn: Turn) = Bundle().apply {
            putInt(KEY_NEXT_ROUND_ID, nextRoundId)
            putString(KEY_NEXT_ROUND_NAME, nextRoundName)
            putParcelable(KEY_CURRENT_TURN, currentTurn)
        }
    }
}

val KEY_NEXT_ROUND_ID = "KEY_NEXT_ROUND_ID"
val KEY_NEXT_ROUND_NAME = "KEY_NEXT_ROUND_NAME"
val KEY_CURRENT_TURN = "KEY_CURRENT_TURN"