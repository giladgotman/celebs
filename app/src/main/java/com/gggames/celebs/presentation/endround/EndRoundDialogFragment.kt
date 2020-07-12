package com.gggames.celebs.presentation.endturn

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.gggames.celebs.R
import com.gggames.celebs.model.Round
import com.gggames.celebs.model.Team
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_end_round_dialog.*

class EndRoundDialogFragment
    : BottomSheetDialogFragment() {

    fun show(activity: AppCompatActivity) {
        show(activity.supportFragmentManager, this.javaClass.simpleName)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_end_round_dialog, container, false)

    private val KEY_ROUND_NAME = "KEY_ROUND_NAME"
    private val KEY_TEAMS = "KEY_TEAMS"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val roundName = it.getString(KEY_ROUND_NAME) ?: ""
            val teams = it.getParcelableArray(KEY_TEAMS) as Array<Team>? ?: emptyArray()

            title.text = getString(R.string.end_round_title, roundName)

            teams.forEachIndexed { index, team ->
                when (index) {
                    0 -> {
                        team1Name.text = team.name
                        team1Score.text = team.score.toString()
                    }
                    1 -> {
                        team2Layout.isVisible = true
                        team2Name.text = team.name
                        team2Score.text = team.score.toString()
                    }
                    2 -> {
                        team3Layout.isVisible = true
                        team3Name.text = team.name
                        team3Score.text = team.score.toString()
                    }
                }
            }
        }

        buttonClose.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }

    companion object {
        fun create(round: Round, teams: List<Team>): EndRoundDialogFragment {
            return EndRoundDialogFragment()
                .apply {
                isCancelable = true
                arguments =
                    Bundle().apply {
                        putString(KEY_ROUND_NAME, round.roundNumber.toString())
                        putParcelableArray(KEY_TEAMS, teams.toTypedArray())
                    }
            }
        }

    }
}
