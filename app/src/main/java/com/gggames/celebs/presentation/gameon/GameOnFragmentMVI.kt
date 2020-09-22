package com.gggames.celebs.presentation.gameon

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.gggames.celebs.R
import com.gggames.celebs.presentation.MainActivity
import com.gggames.celebs.presentation.common.MainActivityDelegate
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent
import io.reactivex.Completable
import io.reactivex.Observable.merge
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * The main fragment in which the game is happening
 */
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
        item.isVisible = true
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_switch_team -> {
                _emitter.onNext(UiEvent.OnSwitchTeamPressed)
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
    }

    override fun onStart() {
        super.onStart()
        val uiEvents = merge(_emitter, (activity as MainActivity).events)
        presenter.states.subscribe { uiBinder.render(it) }.let { disposables.add(it) }

        presenter.bind(merge(uiEvents, uiBinder.events))
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
        presenter.unBind()
    }

    override fun onBackPressed(): Boolean {
        _emitter.onNext(UiEvent.OnBackPressed)
        return true
    }

    override fun onLogout(): Completable =
//        presenter.onLogout()
        Completable.complete()
}
