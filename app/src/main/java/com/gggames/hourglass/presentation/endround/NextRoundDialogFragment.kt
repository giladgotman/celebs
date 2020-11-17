package com.gggames.hourglass.presentation.endturn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.gggames.hourglass.R
import com.gggames.hourglass.model.Round
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
            val nextRound = bundle.getParcelable<Round>(KEY_NEXT_ROUND)!!
            val roundName = when(nextRound.roundNumber) {
                1 -> "Describe"
                2 -> "One Word"
                3 -> "Charades"
                else -> throw IllegalArgumentException("Unknown supported number: ${nextRound.roundNumber}")
            }
            title.text = getString(R.string.next_round_title, nextRound.roundNumber, roundName)

            next_round_description.text = getText(R.string.end_round_round2_description)
            if (nextRound.turn.player != null) {
                val secondsLeft =  nextRound.turn.time?.let { (it / 1000).toInt() % 60 } ?: 0
                next_round_next_player_value.text = getString(
                    R.string.next_round_next_player_value,
                    nextRound.turn.player.name,
                    secondsLeft
                )
            } else {
                next_round_next_player_lbl.isVisible = false
            }
        }
    }
}
