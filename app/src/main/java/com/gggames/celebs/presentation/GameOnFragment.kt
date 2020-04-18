package com.gggames.celebs.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gggames.celebs.R
import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.data.GamesRepositoryImpl
import com.gggames.celebs.data.source.remote.FirebaseGamesDataSource
import com.gggames.celebs.domain.GetGamesUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import com.idagio.app.core.utils.rx.scheduler.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_games.*
import timber.log.Timber


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class GameOnFragment : Fragment() {

    private val TAG = "gilad"

    private lateinit var getGamesUseCase : GetGamesUseCase

    private val scheduler: BaseSchedulerProvider = SchedulerProvider()

    private val disposables = CompositeDisposable()

    private lateinit var playerName: String


    private lateinit var gamesAdapter: GamesAdapter

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_games, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getGamesUseCase = GetGamesUseCase(GamesRepositoryImpl(
            FirebaseGamesDataSource(
                FirebaseFirestore.getInstance()
            )
        ))
        view.findViewById<Button>(R.id.button_fetch).setOnClickListener {
            fetchGames()
        }

        createGameFab.setOnClickListener {
            val args = Bundle()
            args.putString(PLAYER_NAME_KEY, playerName)
            findNavController().navigate(R.id.action_GamesFragment_to_CreateGameFragment, args)
        }

        playerName = GameFlow.me!!.name

        gamesAdapter = GamesAdapter { game ->
            Timber.w("game selected: ${game.name}")
            GameFlow.joinAGame(playerName, game)
            val args = AddCardsFragment.createArgs(game.id, ArrayList(game.teams.map { it.name }), GameFlow.me!!.id)
            findNavController().navigate(R.id.action_GamesFragment_to_AddCardsFragment, args)
        }

        gamesRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this.context)
        gamesRecyclerView.layoutManager = layoutManager
        gamesRecyclerView.itemAnimator = DefaultItemAnimator()
        gamesRecyclerView.adapter = gamesAdapter

    }

    private fun fetchGames() {
        Log.d(TAG, "fetching games")
        button_fetch.text = "fetching..."
        getGamesUseCase().compose(scheduler.applyDefault())
            .subscribe(
                { games ->
                    Timber.d("fetched games: $games")
                    gamesAdapter.setData(games)
                    button_fetch.text = "fetch"
                },
                {
                    button_fetch.text = "fetch"
                }).let { disposables.add(it) }


    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }
}
