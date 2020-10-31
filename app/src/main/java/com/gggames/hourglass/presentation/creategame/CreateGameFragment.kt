package com.gggames.hourglass.presentation.creategame

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
import com.gggames.hourglass.R
import com.gggames.hourglass.features.games.data.MAX_CARDS
import com.gggames.hourglass.model.GameType
import com.gggames.hourglass.model.Team
import com.gggames.hourglass.presentation.MainActivity
import com.gggames.hourglass.presentation.di.ViewComponent
import com.gggames.hourglass.presentation.di.createViewComponent
import com.gggames.hourglass.utils.showErrorToast
import kotlinx.android.synthetic.main.fragment_create_game.*
import javax.inject.Inject

class CreateGameFragment : Fragment(), CreateGamePresenter.View {

    private lateinit var viewComponent: ViewComponent

    @Inject
    lateinit var presenter: CreateGamePresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewComponent = createViewComponent(this)
        viewComponent.inject(this)

        (activity as MainActivity).setTitle(getString(R.string.create_game_fragment_title))
        (activity as MainActivity).setShareVisible(false)

        cardsAmountEditText.setOnEditorActionListener { v, actionId, _ ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_GO) {
                val imm: InputMethodManager = v.context
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                onDoneClick()
                true
            } else false
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

        presenter.bind(this)
    }

    private fun onDoneClick() {
        if (inputValid()) {
            presenter.onDoneClick(getGameDetails())
        }
    }

    private fun getGameDetails(): GameDetails {
        val cardsCount = cardsAmount.editText?.text.toString().toInt()
        val passwordText = password.editText?.text.toString()
        val teams = getTeamsValue()
        val name = gameName.editText?.text.toString()
        val gameType = if (name.contains("gift")) GameType.Gift else GameType.Normal
        return GameDetails(name, teams, cardsCount, passwordText, gameType)
    }
    override fun setDoneEnabled(enabled: Boolean) {
        buttonDone.isEnabled = enabled
    }

    override fun navigateToAddCards(gameId: String) {
        findNavController().navigate(
            R.id.action_CreateGameFragment_to_AddCardsFragment
        )
    }

    override fun showGenericError() {
        showErrorToast(
            requireContext(),
            getString(R.string.error_generic),
            Toast.LENGTH_LONG
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.unBind()
    }

    private fun getTeamsValue(): MutableList<Team> {
        val teams = mutableListOf<Team>()
        if (groupName1.editText?.text?.isNotEmpty() == true) {
            teams.add(Team(name = groupName1.editText?.text.toString()))
        }
        if (groupName2.editText?.text?.isNotEmpty() == true) {
            teams.add(Team(name = groupName2.editText?.text.toString()))
        }
        if (groupName3.editText?.text?.isNotEmpty() == true) {
            teams.add(Team(name = groupName3.editText?.text.toString()))
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
        if (cardsAmount.editText?.text?.isEmpty() == true) {
            cardsAmount.error = "Please enter cards amount"
            return false
        }
        if (cardsAmount.editText?.text?.toString()?.toInt() ?: 0 > MAX_CARDS) {
            cardsAmount.error = "The maximum cards amount is $MAX_CARDS"
            return false
        }
        return true
    }
}
