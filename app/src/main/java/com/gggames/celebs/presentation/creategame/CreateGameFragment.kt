package com.gggames.celebs.presentation.creategame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.celebs.R
import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.features.games.domain.AddGame
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.GameInfo
import com.gggames.celebs.model.GameState
import com.gggames.celebs.model.Team
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import com.gggames.celebs.utils.showErrorToast
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_create_game.*
import timber.log.Timber
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */

class CreateGameFragment : Fragment() {

    private lateinit var viewComponent: ViewComponent

    @Inject
    lateinit var addGame: AddGame
    @Inject
    lateinit var gameFlow: GameFlow

    private val disposables = CompositeDisposable()

    private lateinit var playerName: String

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewComponent = createViewComponent(this)
        viewComponent.inject(this)

        view.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            findNavController().navigate(R.id.action_CreateGame_to_GamesFragment)
        }

        arguments?.let {
            playerName =it.getString(PLAYER_NAME_KEY)!!
        }

        gameName.editText?.addTextChangedListener {
            gameName.error = null
        }

        groupName1.editText?.addTextChangedListener {
            groupName1.error = null
        }
        buttonDone.setOnClickListener {
            if (inputValid()) {
                buttonDone.isEnabled = false
                val now = System.currentTimeMillis()
                val cardsCount = cardsAmount.editText?.text.toString().toInt()
                val teams = getTeamsValue()
                val initialScore = teams.map { it.name to 0 }.toMap()
                val game = Game(
                    "${gameName.editText?.text}$now",
                    gameName.editText?.text.toString(),
                    now,
                    cardsCount,
                    teams,
                    GameState.Created,
                    GameInfo(score = initialScore)
                )

                addGame(game)
                    .subscribe(
                        {
                            Timber.i("gilad game added: ${game.id}")
                            gameFlow.joinAGame(playerName, game)
                            val args = AddCardsFragment.createArgs(
                                game.id,
                                ArrayList(game.teams.map { it.name }),
                                gameFlow.me!!.id
                            )
                            findNavController().navigate(
                                R.id.action_CreateGameFragment_to_AddCardsFragment,
                                args
                            )
                        }, {
                            buttonDone.isEnabled = true
                            showErrorToast(
                                requireContext(),
                                getString(R.string.error_generic),
                                Toast.LENGTH_LONG
                            )
                            Timber.e(it, "gilad game added failed. ${it.localizedMessage}")
                        })
                    .let {
                        disposables.add(it)
                    }
            }
        }
    }

    private fun getTeamsValue(): MutableList<Team> {
        val teams = mutableListOf<Team>()
        if (groupName1.editText?.text?.isNotEmpty() == true) {
            teams.add(Team(groupName1.editText?.text.toString(), emptyList()))
        }
        if (groupName2.editText?.text?.isNotEmpty() == true) {
            teams.add(Team(groupName2.editText?.text.toString(), emptyList()))
        }
        if (groupName3.editText?.text?.isNotEmpty() == true) {
            teams.add(Team(groupName3.editText?.text.toString(), emptyList()))
        }
        return teams
    }

    private fun inputValid(): Boolean {
        if (gameName.editText?.text?.isEmpty() == true) {
            gameName.error = "Please enter game name"
            return false
        }
        if (groupName1.editText?.text?.isEmpty() == true) {
            groupName1.error = "Please enter team name"
            return false
        }

        return true
    }
}
