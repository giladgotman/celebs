package com.gggames.celebs.presentation.creategame

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gggames.celebs.R
import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.features.games.domain.GetGames
import com.gggames.celebs.features.players.domain.JoinGame
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_games.*
import timber.log.Timber
import javax.inject.Inject


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class GamesFragment : Fragment() {

    @Inject
    lateinit var getGames : GetGames

    @Inject
    lateinit var gameFlow: GameFlow

    @Inject
    lateinit var joinGame: JoinGame

    private val disposables = CompositeDisposable()

    private lateinit var playerName: String

    private lateinit var viewComponent: ViewComponent

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

        viewComponent = createViewComponent(this)
        viewComponent.inject(this)

        createGameFab.setOnClickListener {
            val args = Bundle()
            args.putString(PLAYER_NAME_KEY, playerName)
            findNavController().navigate(R.id.action_GamesFragment_to_CreateGameFragment, args)
        }

        playerName = gameFlow.me!!.name

        gamesAdapter =
            GamesAdapter { game ->
                Timber.w("game selected: ${game.name}")

                joinGame(game, gameFlow.me!!)
                    .subscribe({
                        val args = AddCardsFragment.createArgs(
                            game.id,
                            ArrayList(game.teams.map { it.name }),
                            gameFlow.me!!.id
                        )
                        findNavController().navigate(R.id.action_GamesFragment_to_AddCardsFragment, args)
                    }, {
                        Timber.e(it,"error joinGame")
                    })

            }


        itemsswipetorefresh.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this.requireContext(), R.color.colorPrimary))
        itemsswipetorefresh.setColorSchemeColors(Color.WHITE)

        itemsswipetorefresh.setOnRefreshListener {
            fetchGames()
            itemsswipetorefresh.isRefreshing = false
        }



        gamesRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this.context)
        gamesRecyclerView.layoutManager = layoutManager
        gamesRecyclerView.itemAnimator = DefaultItemAnimator()
        gamesRecyclerView.adapter = gamesAdapter


        fetchGames()
    }

    private fun fetchGames() {
        Timber.d("fetching games")
        progress.isVisible = true
        getGames()
            .subscribe(
                { games ->
                    Timber.d("fetched games: $games")
                    progress.isVisible = false
                    gamesAdapter.setData(games)
                },
                {
                    Timber.e(it, "error fetching games")
                    progress.isVisible = false
                }).let { disposables.add(it) }


    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }
}
