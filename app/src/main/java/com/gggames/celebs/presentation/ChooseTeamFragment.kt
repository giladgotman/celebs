package com.gggames.celebs.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.celebs.R
import com.gggames.celebs.core.GameFlow
import kotlinx.android.synthetic.main.fragment_choose_teams.*
import kotlinx.android.synthetic.main.fragment_choose_teams.view.*
import timber.log.Timber

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ChooseTeamFragment : Fragment() {

    lateinit var teams: ArrayList<String>

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_teams, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            teams = it.getStringArrayList(TEAMS_KEY)!!
        }

        teams.forEachIndexed{ index, group->
            when (index) {
                0 -> {
                    teamRadioGroup.radioButtonTeam1.text = group
                }
                1 -> {
                    teamRadioGroup.radioButtonTeam2.text = group
                    teamRadioGroup.radioButtonTeam2.isVisible = true
                }
                2 -> {
                    teamRadioGroup.radioButtonTeam3.text = group
                    teamRadioGroup.radioButtonTeam3.isVisible = true
                }
            }
        }

        view.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            activity?.onBackPressed()
        }
        buttonDone.setOnClickListener {
            buttonDone.isEnabled = false
            val selection = teamRadioGroup.checkedRadioButtonId
            val button = view.findViewById<RadioButton>(selection)
            val teamName = button.text.toString()
            Timber.w("selected team: $selection, team: $teamName")

            GameFlow.chooseAteam(teamName)

            findNavController().navigate(R.id.action_chooseTeamFragment_to_gameOnFragment)
        }


    }
}

const val TEAMS_KEY = "TEAMS_KEY"
