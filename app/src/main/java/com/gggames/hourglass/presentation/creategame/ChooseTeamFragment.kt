package com.gggames.hourglass.presentation.creategame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.hourglass.R
import com.gggames.hourglass.core.Authenticator
import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.features.players.domain.ChooseTeam
import com.gggames.hourglass.presentation.di.ViewComponent
import com.gggames.hourglass.presentation.di.createViewComponent
import com.gggames.hourglass.utils.showErrorToast
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_choose_teams.*
import kotlinx.android.synthetic.main.fragment_choose_teams.view.*
import timber.log.Timber

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ChooseTeamFragment : Fragment() {

    private lateinit var viewComponent: ViewComponent
    private val disposables = CompositeDisposable()
    @Inject
    lateinit var chooseTeam: ChooseTeam
    @Inject
    lateinit var authenticator: Authenticator
    @Inject lateinit var gamesRepository: GamesRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choose_teams, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewComponent = createViewComponent(this)
        viewComponent.inject(this)

        buttonDone.isVisible = true

        gamesRepository.currentGame!!.teams.forEachIndexed { index, team ->
            when (index) {
                0 -> {
                    teamRadioGroup.radioButtonTeam1.text = team.name
                }
                1 -> {
                    teamRadioGroup.radioButtonTeam2.text = team.name
                    teamRadioGroup.radioButtonTeam2.isVisible = true
                }
                2 -> {
                    teamRadioGroup.radioButtonTeam3.text = team.name
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

            gamesRepository.currentGame?.let { game ->
                chooseTeam(game.id, authenticator.me!!, teamName)
                    .subscribe({
                        authenticator.setMyTeam(teamName)
                        Timber.w("ggg you choose team : $teamName")
                    }, { e ->
                        buttonDone.isEnabled = true
                        showErrorToast(requireContext(), getString(R.string.error_generic), Toast.LENGTH_LONG)
                        Timber.e(e, "ggg failed to choose team : $teamName")
                    }).let { disposables.add(it) }
            }

            findNavController().navigate(R.id.action_chooseTeamFragment_to_gameOnFragment)
        }
    }
}
