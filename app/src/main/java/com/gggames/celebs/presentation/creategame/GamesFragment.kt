package com.gggames.celebs.presentation.creategame

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gggames.celebs.R
import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.features.games.domain.GetGames
import com.gggames.celebs.features.games.domain.ObserveGame
import com.gggames.celebs.features.players.domain.JoinGame
import com.gggames.celebs.model.Game
import com.gggames.celebs.presentation.MainActivity
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import com.gggames.celebs.utils.showInfoToast
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
    lateinit var observeGame: ObserveGame

    @Inject
    lateinit var gameFlow: GameFlow

    @Inject
    lateinit var joinGame: JoinGame

    private val disposables = CompositeDisposable()

    private lateinit var viewComponent: ViewComponent

    private lateinit var gamesAdapter: GamesAdapter

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_games, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewComponent = createViewComponent(this)
        viewComponent.inject(this)

        val playerName = gameFlow.me?.name
        if (playerName == null) {
            arguments?.getString("gameId")?.let {
                showInfoToast(requireContext(),"Please login and then use the link to the game", Toast.LENGTH_LONG)
            }
            logout()
            return
        }
        createGameFab.setOnClickListener {
            findNavController().navigate(R.id.action_GamesFragment_to_CreateGameFragment)
        }

        (activity as MainActivity).setTitle("Games")
        (activity as MainActivity).setShareVisible(false)

        gamesAdapter =
            GamesAdapter { game ->
                Timber.w("game selected: ${game.name}")
                joinGameAndGoToAddCards(game)
            }


        itemsswipetorefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                this.requireContext(),
                R.color.colorPrimary
            )
        )
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

        arguments?.getString("gameId")?.let { gameId ->
            arguments?.remove("gameId")
            observeGame(gameId).take(1).subscribe({
                joinGameAndGoToAddCards(it)
            }, {
                Timber.e(it, "Error trying to joing game: $gameId")
            }).let { disposables.add(it) }
        } ?: fetchGames()

    }

    private fun logout() {
        requireActivity().finish()
        gameFlow.logout()
    }

    private fun joinGameAndGoToAddCards(game: Game) {
        joinGame(game, gameFlow.me!!)
            .subscribe({
                findNavController().navigate(R.id.action_GamesFragment_to_AddCardsFragment)
            }, {
                Timber.e(it, "error joinGame")
            }).let { disposables.add(it) }
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
