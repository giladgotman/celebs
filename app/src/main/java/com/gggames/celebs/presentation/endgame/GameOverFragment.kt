package com.gggames.celebs.presentation.endgame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import com.gggames.celebs.R
import com.gggames.celebs.model.Card
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import com.gggames.celebs.presentation.endgame.GameOverScreenContract.Trigger
import com.gggames.celebs.presentation.endgame.GameOverScreenContract.UiEvent.PressedFinish
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_game_over.*
import timber.log.Timber

class GameOverFragment : Fragment() {

    @Inject
    lateinit var presenter: GameOverScreenContract.Presenter
    private lateinit var viewComponent: ViewComponent
    private val teamsAdapter = TeamsAdapter()
    private val cardsAdapter = CardsAdapter(::onInfoClick)
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

    private fun onInfoClick(card: Card) {
        Timber.w("info click : ${card.name}")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
        presenter.unBind()
    }
}
