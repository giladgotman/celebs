package com.gggames.hourglass.presentation.gameon

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.hourglass.R
import com.gggames.hourglass.presentation.MainActivity
import com.gggames.hourglass.presentation.common.MainActivityDelegate
import com.gggames.hourglass.presentation.di.ViewComponent
import com.gggames.hourglass.presentation.di.createViewComponent
import com.gggames.hourglass.presentation.gameon.GameScreenContract.UiEvent
import io.reactivex.Completable
import io.reactivex.Observable.merge
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

/**
 * The main fragment in which the game is happening
 */

const val GAME_ID_KEY = "gameId"
class GameOnFragmentMVI : Fragment(),
    MainActivityDelegate {

    private lateinit var viewComponent: ViewComponent

    @Inject
    lateinit var presenter: GamePresenterMVI

    @Inject
    lateinit var uiBinder: GameOnUiBinder

    private val _emitter = PublishSubject.create<UiEvent>()

    private val disposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_game_on, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val item = menu.findItem(R.id.menu_switch_team)
        val itemEndTurn = menu.findItem(R.id.menu_end_turn)
        item.isVisible = true
        itemEndTurn.isVisible = true
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        uiBinder.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_switch_team -> {
                _emitter.onNext(UiEvent.OnSwitchTeamPressed)
                true
            }
            R.id.menu_end_turn -> {
                _emitter.onNext(UiEvent.EndTurnClick)
                true
            }
            else -> false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewComponent = createViewComponent(this)
        viewComponent.inject(this)

        uiBinder.setFragment(this)

        val uiEvents = merge(_emitter, (activity as MainActivity).events)
        presenter.states.subscribe { uiBinder.render(it) }.let { disposables.add(it) }

        presenter.bind(merge(uiEvents, uiBinder.events))
        presenter.triggers.subscribe { uiBinder.trigger(it) }.let { disposables.add(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d("onDestroyView")
        disposables.clear()
        presenter.unBind()
        uiBinder.clear()
    }


    fun navigateToEndGame() {
        disposables.clear()
        findNavController().navigate(
            R.id.action_gameOnFragment_to_gameOverFragment,
            Bundle().apply {
                putString(GAME_ID_KEY, "dummy game id")
            })
    }

    fun navigateToGames() {
        disposables.clear()
        findNavController().navigate(R.id.action_gameOnFragment_to_GamesFragment)
    }

    fun navigateToTeams() {
        disposables.clear()
        findNavController().navigate(R.id.action_gameOnFragment_to_ChooseTeamFragment)
    }

    override fun onBackPressed(): Boolean {
        _emitter.onNext(UiEvent.OnBackPressed)
        return true
    }

    override fun onLogout(): Completable =
//        presenter.onLogout()
        Completable.complete()
}
