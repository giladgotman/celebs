package com.gggames.celebs.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gggames.celebs.R
import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.data.cards.CardsRepositoryImpl
import com.gggames.celebs.data.model.Card
import com.gggames.celebs.data.model.Player
import com.gggames.celebs.data.players.PlayersRepositoryImpl
import com.gggames.celebs.data.source.remote.FirebaseCardsDataSource
import com.gggames.celebs.data.source.remote.FirebasePlayersDataSource
import com.gggames.celebs.domain.cards.ObserveAllCards
import com.gggames.celebs.domain.players.ObservePlayers
import com.google.firebase.firestore.FirebaseFirestore
import com.idagio.app.core.utils.rx.scheduler.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_game_on.*
import timber.log.Timber


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class GameOnFragment : Fragment() {

    private val TAG = "gilad"

    private lateinit var playersObservable: ObservePlayers

    private lateinit var cardsObservable: ObserveAllCards

    private val disposables = CompositeDisposable()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_on, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gameId = GameFlow.currentGame!!.id

        playersObservable = ObservePlayers(
            PlayersRepositoryImpl(
                FirebasePlayersDataSource(
                    FirebaseFirestore.getInstance()
                )
            ),
            SchedulerProvider()
        )

        cardsObservable = ObserveAllCards(
            CardsRepositoryImpl(
                FirebaseCardsDataSource(
                    gameId,
                    FirebaseFirestore.getInstance()
                )
            ),
            SchedulerProvider()
        )

        playersObservable(gameId)
            .distinctUntilChanged()
            .subscribe({list->
                updateTeams(list)
            }, {
                Timber.e(it, "error while observing players")
            }).let {
                disposables.add(it)
            }

        cardsObservable()
            .distinctUntilChanged()
            .subscribe({cards->
                updateCards(cards)
            }, {
                Timber.e(it, "error while observing cards")
            }).let {
                disposables.add(it)
            }

        startButton.setOnClickListener {

        }


    }

    private fun updateCards(cards: List<Card>) {
        cardsAmount.text = cards.size.toString()
    }

    private fun updateTeams(list: List<Player>) {
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
        disposables.clear()
    }
}
