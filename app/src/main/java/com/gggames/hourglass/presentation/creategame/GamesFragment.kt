package com.gggames.hourglass.presentation.creategame

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gggames.hourglass.R
import com.gggames.hourglass.model.Game
import com.gggames.hourglass.presentation.MainActivity
import com.gggames.hourglass.presentation.di.ViewComponent
import com.gggames.hourglass.presentation.di.createViewComponent
import com.gggames.hourglass.presentation.gameon.GAME_ID_KEY
import com.gggames.hourglass.presentation.instructions.InstructionsDialogFragment
import com.gggames.hourglass.utils.showErrorToast
import com.gggames.hourglass.utils.showInfoToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_games.*
import timber.log.Timber
import javax.inject.Inject

class GamesFragment : Fragment(), GamesPresenter.View {

    @Inject
    lateinit var presenter: GamesPresenter
    private lateinit var viewComponent: ViewComponent
    private lateinit var gamesAdapter: GamesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_games, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewComponent = createViewComponent(this)
        viewComponent.inject(this)

        createGameFab.setOnClickListener {
            findNavController().navigate(R.id.action_GamesFragment_to_CreateGameFragment)
        }

        (activity as MainActivity).setTitle(getString(R.string.games_fragment_title))
        (activity as MainActivity).setShareVisible(false)

        gamesAdapter =
            GamesAdapter { game ->
                presenter.onGameClick(game)
            }

        itemsswipetorefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                this.requireContext(),
                R.color.colorPrimary
            )
        )
        itemsswipetorefresh.setColorSchemeColors(Color.WHITE)

        itemsswipetorefresh.setOnRefreshListener {
            presenter.onRefresh()
            itemsswipetorefresh.isRefreshing = false
        }

        gamesRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this.context)
        gamesRecyclerView.layoutManager = layoutManager
        gamesRecyclerView.itemAnimator = DefaultItemAnimator()
        gamesRecyclerView.adapter = gamesAdapter

        var gameIdFromDeepLink: String? = null

        arguments?.getString(GAME_ID_KEY)?.let { gameId ->
            arguments?.remove(GAME_ID_KEY)
            gameIdFromDeepLink = gameId
        }
        Timber.w("ggg bind, gameIdFromDeepLink: $gameIdFromDeepLink")
        presenter.bind(this, gameIdFromDeepLink)
    }

    override fun show(games: List<Game>) {
        gamesAdapter.setData(games)
    }

    override fun showNoGamesView(visible: Boolean) {
        if (visible) {
            noGamesView.visibility = View.VISIBLE
        } else {
            noGamesView.visibility = View.GONE
        }
    }

    override fun showGenericError() {
        showErrorToast(
            requireContext(),
            getString(R.string.error_generic),
            Toast.LENGTH_LONG
        )
    }

    override fun showNeedLoginInfo() {
        showInfoToast(requireContext(), "Please login and then use the link to the game", Toast.LENGTH_LONG)
    }

    override fun showApproveJoinGame(game: Game) {
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    presenter.onUserJoinGameResponse(game, true)
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                    presenter.onUserJoinGameResponse(game, false)
                }
            }
        }
        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.celebs_MaterialAlertDialog)
        builder
            .setTitle(R.string.games_join_game_dialog_title)
            .setMessage(getString(R.string.games_join_game_dialog_subtitle, game.name))
            .setPositiveButton(R.string.games_join_game_dialog_positive_button, dialogClickListener)
            .setNegativeButton(R.string.games_join_game_dialog_negative_button, dialogClickListener)
            .show()
    }

    override fun showLoading(show: Boolean) {
        progress.isVisible = show
    }

    override fun finishScreen() {
        requireActivity().finish()
    }

    override fun showJoinedGameIsFinished(gameName: String) {
        showInfoToast(requireContext(), "The game '$gameName' is already over")
    }

    override fun navigateToAddCards() {
        findNavController().navigate(R.id.action_GamesFragment_to_AddCardsFragment)
    }

    override fun navigateToGameOver(gameId: String) {
        findNavController().navigate(R.id.action_GamesFragment_to_GameOverFragment, Bundle().apply {
            putString(GAME_ID_KEY, gameId)
        })
    }

    override fun showHelp() {
        InstructionsDialogFragment().show(requireActivity() as AppCompatActivity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.unBind()
    }
}
