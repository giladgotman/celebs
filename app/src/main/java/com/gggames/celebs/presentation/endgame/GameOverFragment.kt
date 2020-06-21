package com.gggames.celebs.presentation.endgame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.celebs.R
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import com.gggames.celebs.presentation.endgame.GameOverScreenContract.Trigger
import com.gggames.celebs.presentation.endgame.GameOverScreenContract.UiEvent.PressedFinish
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_game_over.*
import javax.inject.Inject

class GameOverFragment : Fragment() {

    @Inject
    lateinit var presenter: GameOverScreenContract.Presenter
    private lateinit var viewComponent: ViewComponent

    private val events = PublishSubject.create<GameOverScreenContract.UiEvent>()
    private val disposables = CompositeDisposable()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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
        presenter.bind(events, gameId!!)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.unBind()
    }
}