package com.gggames.celebs.presentation.creategame

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.celebs.R
import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.features.players.domain.JoinGame
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
    lateinit var setGame: SetGame
    @Inject
    lateinit var gameFlow: GameFlow
    @Inject
    lateinit var joinGame: JoinGame

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

        cardsAmountEditText.setOnEditorActionListener { v, actionId, _ ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_GO) {
                val imm: InputMethodManager = v.context
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                onDoneClick()
                true
            } else false
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
            onDoneClick()
        }
    }

    private fun onDoneClick() {
        if (inputValid()) {
            buttonDone.isEnabled = false
            val game = getGameDetails()
            joinGame(game)
        }
    }

    private fun getGameDetails(): Game {
        val now = System.currentTimeMillis()
        val cardsCount = cardsAmount.editText?.text.toString().toInt()
        val passwordText = password.editText?.text.toString()
        val teams = getTeamsValue()
        val initialScore = teams.map { it.name to 0 }.toMap()
        val game = Game(
            "${gameName.editText?.text}$now",
            gameName.editText?.text.toString(),
            now,
            passwordText,
            cardsCount,
            teams,
            GameState.Created,
            GameInfo(score = initialScore)
        )
        return game
    }

    private fun joinGame(game: Game) {
        setGame(game)
            .andThen(joinGame(game, gameFlow.me!!))
            .subscribe(
                {
                    Timber.i("${gameFlow.me!!.name} created and joined game: ${game.id}")
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
                    Timber.e(it, "game add and join failed.")
                })
            .let {
                disposables.add(it)
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
