package com.gggames.celebs.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gggames.celebs.R
import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.data.PlayersRepositoryImpl
import com.gggames.celebs.data.model.Player
import com.gggames.celebs.data.source.remote.FirebasePlayersDataSource
import com.gggames.celebs.domain.ObservePlayers
import com.google.firebase.firestore.FirebaseFirestore
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
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

    private val scheduler: BaseSchedulerProvider = SchedulerProvider()

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

        playersObservable = ObservePlayers(
            PlayersRepositoryImpl(
                FirebasePlayersDataSource(
                FirebaseFirestore.getInstance()
            )
            )
        )
        val gameId = GameFlow.currentGame!!.id
        playersObservable(gameId)
            .distinctUntilChanged()
            .subscribe({list->
                val teams = list.groupBy { it.team }
                val team1 = teams.keys.toList()[0]
                Timber.w("team1 : ${teams.keys.toList()[0]}")
                Timber.w("team2 : ${teams.keys.toList()[1]}")
                team1?.let {
                updateTeam(team1, teams[0]?.toList() ?: emptyList())
                }
            }, {
                Timber.e(it, "error while observing players")
            }).let {
                disposables.add(it)
            }

    }

    fun updateTeam(teamName: String, players: List<Player>) {
        Timber.w("updateTeam : teamName: $teamName , p: ${players.size}")
        team1Name.text = teamName
        val sb = StringBuilder()
        players.forEach {
            sb.append(it.name)
            sb.append(", ")
        }
        team1Value.text = sb.toString()
    }

    fun updateTea2(teamName: String, players: List<Player>) {
        team2Name.text = teamName
        val sb = StringBuilder()
        players.forEach {
            sb.append(it.name)
            sb.append(", ")
        }
        team2Value.text = sb.toString()
    }


    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}
