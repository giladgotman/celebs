package com.gggames.celebs.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.gggames.celebs.R
import com.gggames.celebs.data.model.Card
import com.gggames.celebs.data.model.Player
import kotlinx.android.synthetic.main.fragment_game_on.*
import timber.log.Timber


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class GameOnFragment : Fragment(), GamePresenter.GameView {

    private val TAG = "gilad"

    lateinit var presenter: GamePresenter
    var gameRound = 1


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        presenter = GamePresenter()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_on, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter.bind(this)
        cardTextView.text = ""
        startButton.isVisible = true

        startButton.setOnClickListener {
            presenter.onPlayerStarted()
        }

        reloadButton.setOnClickListener {
            presenter.onReloadDeck()
            gameRound++
            if (gameRound > 3) {
                gameRound = 1
            }
            round.text = gameRound.toString()
        }

        correctButton.setOnClickListener {
            pickNextCard()
        }
    }

    private fun pickNextCard() {
        presenter.onPickNextCard()
    }

    override fun updateCards(cards: List<Card>) {
        cardsAmount.text = cards.size.toString()
    }

    override fun updateCard(card: Card) {
        Timber.w("ggg update card: $card")
        startButton.isVisible = false
        cardTextView.text = card.name
    }

    override fun updateTeams(list: List<Player>) {
        val teams = list.groupBy { it.team }
        val teamList = teams.keys.toList().filterNotNull()
        teamList.forEachIndexed { index, teamName ->
            val players = teams[teamName]?.toList() ?: emptyList()
            when (index) {
                0 -> updateTeam1(teamName, players)
                1 -> updateTeam2(teamName, players)
            }
        }
    }

    private fun updateTeam1(teamName: String, players: List<Player>) {
        Timber.w("updateTeam : teamName: $teamName , p: ${players.size}")
        team1Name.text = "$teamName : "
        val sb = StringBuilder()
        players.forEach {
            sb.append(it.name)
            sb.append(", ")
        }
        team1Value.text = sb.toString()
    }

    private fun updateTeam2(teamName: String, players: List<Player>) {
        Timber.w("updateTeam2 : teamName: $teamName , p: ${players.size}")
        team2Name.text = "$teamName : "
        val sb = StringBuilder()
        players.forEach {
            sb.append(it.name)
            sb.append(", ")
        }
        team2Value.text = sb.toString()
    }

    //todo add update Team3


    override fun onDestroy() {
        super.onDestroy()
        presenter.unBind()
    }
}
