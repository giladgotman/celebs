package com.gggames.hourglass.presentation.endturn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.gggames.hourglass.R
import com.gggames.hourglass.model.Team
import kotlinx.android.synthetic.main.fragment_end_round_dialog.*

class EndRoundDialogFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_end_round_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val prevRoundName = it.getString(KEY_PREV_ROUND_NAME)
            val teams = it.getParcelableArray(KEY_TEAMS) as Array<Team>? ?: emptyArray()

            title.text = getString(R.string.end_round_title, prevRoundName)

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
    }
}
