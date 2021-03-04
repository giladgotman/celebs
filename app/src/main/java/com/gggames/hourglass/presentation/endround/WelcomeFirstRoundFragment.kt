package com.gggames.hourglass.presentation.endturn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.gggames.hourglass.R
import com.gggames.hourglass.model.Player
import com.gggames.hourglass.model.PlayerTurnState
import com.gggames.hourglass.presentation.common.NameBadge
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_welcome_first_round.*

class WelcomeFirstRoundFragment : BottomSheetDialogFragment() {

    fun show(activity: AppCompatActivity) {
        show(activity.supportFragmentManager, this.javaClass.simpleName)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_welcome_first_round, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            val nextRoundId = bundle.getInt(KEY_NEXT_ROUND_ID)
            val nextRoundName = bundle.getString(KEY_NEXT_ROUND_NAME)
            val nextPlayer = bundle.getParcelable<Player>(KEY_NEXT_PLAYER)
            title.text = getString(R.string.next_round_title, nextRoundId, nextRoundName)

            next_round_description.text = when (nextRoundId) {
                1 -> getText(R.string.instructions_section3_round1_descriptions)
                2 -> getText(R.string.instructions_section3_round2_descriptions)
                3 -> getText(R.string.instructions_section3_round3_descriptions)
                else -> ""
            }

            if (nextPlayer != null) {
                next_player_name_badge.state =
                    NameBadge.State(nextPlayer.name, nextPlayer.playerTurnState ?: PlayerTurnState.Idle)
            } else {
                starting_player_lbl.isVisible = false
                next_player_name_badge.isVisible = false
            }
        }
        button_ready.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun newInstance(nextRoundId: Int, nextRoundName: String, nextPlayer: Player?) =
            WelcomeFirstRoundFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_NEXT_ROUND_ID, nextRoundId)
                    putString(KEY_NEXT_ROUND_NAME, nextRoundName)
                    nextPlayer?.let {
                        putParcelable(KEY_NEXT_PLAYER, nextPlayer)
                    }
                }
            }

        const val KEY_NEXT_PLAYER = "KEY_NEXT_PLAYER"
    }
}


