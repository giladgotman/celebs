package com.gggames.celebs.presentation.endgame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import com.gggames.celebs.R
import com.gggames.celebs.features.video.VideoPlayer
import com.gggames.celebs.model.Card
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import com.gggames.celebs.presentation.endgame.GameOverScreenContract.Trigger
import com.gggames.celebs.presentation.endgame.GameOverScreenContract.UiEvent.PressedFinish
import com.google.android.exoplayer2.ui.PlayerView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_game_over.*
import timber.log.Timber
import javax.inject.Inject

class GameOverFragment : Fragment() {

    @Inject
    lateinit var presenter: GameOverScreenContract.Presenter

    @Inject
    lateinit var videoPlayer: VideoPlayer

    private lateinit var viewComponent: ViewComponent
    private val teamsAdapter = TeamsAdapter()
    private val cardsAdapter = CardsAdapter(::onCardClick)
    private val teamsAndCardsAdapter = MergeAdapter(teamsAdapter, cardsAdapter)

        private val events = PublishSubject.create<GameOverScreenContract.UiEvent>()
    private val disposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_over, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewComponent = createViewComponent(this)
        viewComponent.inject(this)

        val gameId: String? = arguments?.getString("gameId")

        videoPlayer.initializePlayer()


        presenter.states.subscribe { render(it) }.let { disposables.add(it) }
        presenter.triggers.subscribe { trigger(it) }.let { disposables.add(it) }

        finishButton.setOnClickListener {
            events.onNext(PressedFinish)
        }

        setupRecyclerView()
        presenter.bind(events, gameId!!)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this.context)
        teamsAndCardsRv.layoutManager = layoutManager
        teamsAndCardsRv.itemAnimator = DefaultItemAnimator()
        teamsAndCardsRv.adapter = teamsAndCardsAdapter
    }

    private fun trigger(trigger: Trigger) {
        when (trigger) {
            Trigger.NavigateToGames -> navigateToGames()
        }
    }

    private fun navigateToGames() {
        findNavController().navigate(R.id.action_gameOverFragment_to_GamesFragment)
    }

    private fun render(state: GameOverScreenContract.State) {
        subtitle.text = getString(R.string.game_over_subtitle, state.winningTeam)
        teamsAdapter.submitList(state.teams)
        cardsAdapter.submitList(state.cards)
    }

    private fun onCardClick(card: Card, playerView: PlayerView, giftText: TextView) {
        Timber.w("info click : ${card.name}")
        val url = card.videoUrlFull
        url?.let {

            if (it.startsWith("text:")) {
                if (giftText.tag != "open") {
                    giftText.text = it.removePrefix("text:")
                    giftText.tag = "open"
                } else {
                    giftText.text = card.name
                    giftText.tag = null
                }
            } else {
                videoPlayer.setView(playerView)
                playerView.isVisible = true
                videoPlayer.playVideo(it)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
        videoPlayer.releasePlayer()
        presenter.unBind()
    }
}
