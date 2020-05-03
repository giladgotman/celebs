package com.gggames.celebs.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.celebs.R
import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.data.players.PlayersRepositoryImpl
import com.gggames.celebs.data.source.remote.FirebasePlayersDataSource
import com.gggames.celebs.domain.players.ChooseTeam
import com.gggames.celebs.utils.showErrorToast
import com.google.firebase.firestore.FirebaseFirestore
import com.idagio.app.core.utils.rx.scheduler.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_choose_teams.*
import kotlinx.android.synthetic.main.fragment_choose_teams.view.*
import timber.log.Timber

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ChooseTeamFragment : Fragment() {

    private val disposables = CompositeDisposable()
    lateinit var teams: ArrayList<String>
    private lateinit var chooseTeam: ChooseTeam

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_teams, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonDone.isVisible = true

        chooseTeam = ChooseTeam(
            PlayersRepositoryImpl(
                FirebasePlayersDataSource(
                    FirebaseFirestore.getInstance()
                )
            ),
            SchedulerProvider()
        )

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

        buttonDone.setOnClickListener {
            buttonDone.isEnabled = false
            val selection = teamRadioGroup.checkedRadioButtonId
            val button = view.findViewById<RadioButton>(selection)
            val teamName = button.text.toString()
            Timber.w("selected team: $selection, team: $teamName")

            GameFlow.currentGame?.let {
                chooseTeam(it.id, GameFlow.me!!, teamName)
                    .subscribe({
                        GameFlow.setMyTeam(teamName)
                        Timber.w("ggg you chosed team : $teamName")
                    },{e->
                        buttonDone.isEnabled = true
                        showErrorToast(requireContext(), getString(R.string.error_generic), Toast.LENGTH_LONG)
                        Timber.e(e, "ggg failed to choose team : $teamName")
                    }).let { disposables.add(it) }
            }

            findNavController().navigate(R.id.action_chooseTeamFragment_to_gameOnFragment)
        }


    }
}

const val TEAMS_KEY = "TEAMS_KEY"
