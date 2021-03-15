package com.gggames.hourglass.presentation.endturn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.gggames.hourglass.R
import com.gggames.hourglass.model.PlayerTurnState
import com.gggames.hourglass.model.Turn
import com.gggames.hourglass.presentation.common.NameBadge
import kotlinx.android.synthetic.main.fragment_next_round.*
import kotlin.math.ceil

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

            next_round_description.text = when (nextRoundId) {
                1 -> getText(R.string.instructions_section3_round1_descriptions)
                2 -> getText(R.string.instructions_section3_round2_descriptions)
                3 -> getText(R.string.instructions_section3_round3_descriptions)
                else -> ""
            }


            if (currentTurn?.player != null) {
                next_player_name_badge.state =
                    NameBadge.State(currentTurn.player.name, PlayerTurnState.UpNext)
                val secondsLeft = convertTime(currentTurn.time)
                next_round_next_player_value.text = getString(
                    R.string.next_round_next_player_value,
                    secondsLeft
                )
            } else {
                starting_player_lbl.isVisible = false
                next_player_name_badge.isVisible = false
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

    @VisibleForTesting
    fun convertTime(time: Long?) =
        time?.let {
            ceil((it / 1000f)).toInt()
        } ?: 0
}

val KEY_NEXT_ROUND_ID = "KEY_NEXT_ROUND_ID"
val KEY_NEXT_ROUND_NAME = "KEY_NEXT_ROUND_NAME"
val KEY_CURRENT_TURN = "KEY_CURRENT_TURN"