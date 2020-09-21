package com.gggames.celebs.presentation.endgame

import android.graphics.Color
import android.graphics.Rect
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
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import javax.inject.Inject

class GameOverFragment : Fragment() {

    @Inject
    lateinit var presenter: GameOverScreenContract.Presenter

    @Inject
    lateinit var videoPlayer: VideoPlayer

    private lateinit var viewComponent: ViewComponent
    private val teamsAdapter = TeamsAdapter()
    private val cardsAdapter = CardsAdapter(::onCardClick, ::onClose)


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
            is Trigger.NavigateToGames -> navigateToGames()
            is Trigger.StartKonffeti -> startKonffeti()
            is Trigger.ShowVideoAndKonffeti -> showVideoAndKonffeti(trigger.card, trigger.playerView, trigger.giftText)
        }
    }

    private fun showVideoAndKonffeti(card: Card, playerView: PlayerView, giftText: TextView) {
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
            val myViewRect = Rect()
            giftText.getGlobalVisibleRect(myViewRect)
            val x = myViewRect.left.toFloat()
            val y = myViewRect.top.toFloat()
            burstKonffeti(x,y)
        }
    }

    private fun startKonffeti() {
        viewKonfetti.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 5f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .addShapes(Shape.Square, Shape.Circle)
            .addSizes(Size(12))
            .setPosition(-50f, viewKonfetti.width + 50f, -50f, -50f)
            .streamFor(300, 3000L)
    }

    private fun burstKonffeti(x: Float, y: Float) {
        viewKonfetti.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 5f)
            .setFadeOutEnabled(true)
            .setTimeToLive(1000L)
            .addShapes(Shape.Circle)
            .addSizes(Size(12), Size(16, 6f))
            .setPosition(x,y)
            .burst(100)
    }

    private fun navigateToGames() {
        findNavController().navigate(R.id.action_gameOverFragment_to_GamesFragment)
    }

    private fun render(state: GameOverScreenContract.State) {
        subtitle.text = getString(R.string.game_over_subtitle, state.winningTeam)
        teamsAdapter.submitList(state.teams)
        cardsAdapter.submitList(state.cards)
        state.mainTitle?.let {
            title.text = it
        }
        state.subTitle?.let {
            subtitle.text = it
        }
    }

    private fun onCardClick(card: Card, playerView: PlayerView, giftText: TextView) {
        events.onNext(GameOverScreenContract.UiEvent.PressedCard(card, playerView, giftText))
    }

    private fun onClose(playerView: PlayerView) {
        playerView.isVisible = false
        videoPlayer.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
        videoPlayer.releasePlayer()
        presenter.unBind()
    }
}
