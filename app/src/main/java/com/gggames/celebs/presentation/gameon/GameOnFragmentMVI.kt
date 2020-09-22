package com.gggames.celebs.presentation.gameon

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gggames.celebs.R
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Player
import com.gggames.celebs.model.Round
import com.gggames.celebs.model.Team
import com.gggames.celebs.presentation.MainActivity
import com.gggames.celebs.presentation.common.MainActivityDelegate
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import com.gggames.celebs.presentation.endturn.EndRoundDialogFragment
import com.gggames.celebs.presentation.endturn.KEY_CARDS
import com.gggames.celebs.presentation.endturn.KEY_PLAYER_NAME
import com.gggames.celebs.presentation.endturn.KEY_ROUND_NUMBER
import com.gggames.celebs.presentation.gameon.GameScreenContract.ButtonState
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent.RoundClick
import com.gggames.celebs.utils.showInfoToast
import io.reactivex.Completable
import io.reactivex.Observable.merge
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_game_on.*
import java.util.*
import javax.inject.Inject

/**
 * The main fragment in which the game is happening
 */
class GameOnFragmentMVI : Fragment(),
    MainActivityDelegate {

    private lateinit var viewComponent: ViewComponent

    @Inject
    lateinit var presenter: GamePresenterMVI

    private var mCountDownTimer: CountDownTimer? = null

    private var mTimeLeftInMillis = TURN_TIME_MILLIS

    private val _emitter = PublishSubject.create<UiEvent>()

    private var playerAdapters: List<PlayersAdapter> =
        listOf(PlayersAdapter(), PlayersAdapter(), PlayersAdapter())

    private lateinit var playersRecycleViews: List<RecyclerView>

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

        cardTextView.text = ""

        endTurnButton.setOnClickListener {
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        timerTextView.text = "Turn Ended"
                        _emitter.onNext(UiEvent.EndTurnClick)
                    }

                    DialogInterface.BUTTON_NEGATIVE -> {
                        startTimer()
                    }
                }
            }
            mCountDownTimer?.cancel()
            val builder = AlertDialog.Builder(context)
            builder.setMessage(getString(R.string.end_turn_alert_message))
                .setPositiveButton(getString(R.string.ok), dialogClickListener)
                .setNegativeButton(getString(R.string.cancel), dialogClickListener)
                .show()
        }

        correctButton.setOnClickListener {
            _emitter.onNext(UiEvent.CorrectClick(mTimeLeftInMillis))
        }

        roundTextView.setOnClickListener {
            _emitter.onNext(RoundClick(mTimeLeftInMillis))
        }

        startButton.setOnClickListener {
            _emitter.onNext(UiEvent.StartStopClick(startButton.state, mTimeLeftInMillis))
        }

        cardsAmount.setOnClickListener {
            _emitter.onNext(UiEvent.CardsAmountClick)
        }

        playersRecycleViews =
            listOf(team1players, team2players, team3players)

        playersRecycleViews.forEachIndexed { index, recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(this.context)
            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = playerAdapters[index]
        }
        setupTimer()
    }

    override fun onStart() {
        super.onStart()
        val uiEvents = merge(_emitter, (activity as MainActivity).events)
        presenter.states.subscribe({
            render(it)
        },
            {})

        presenter.bind(uiEvents)
    }

    private fun render(state: GameScreenContract.State) {
        cardTextView.text = state.currentCard?.name ?: ""
    }

    override fun onStop() {
        super.onStop()
        clear()
    }

    var endRoundDialogFragment: EndRoundDialogFragment? = null

    private fun updateTime(time: Long) {
        mTimeLeftInMillis = time
        timerTextView?.text = getFormattedTime()
    }

    private fun startTimer() {
        mCountDownTimer?.cancel()
        mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onFinish() {
                _emitter.onNext(UiEvent.TimerEnd)
            }

            override fun onTick(millis: Long) {
                updateTime(millis)
            }
        }.start()
    }

    private fun updateTeam1(teamName: String, players: List<Player>) {
        team1Name.text = "$teamName"
        team1Layout.isVisible = true
        playerAdapters[0].setData(players)
    }

    private fun updateTeam2(teamName: String, players: List<Player>) {
        team2Name.text = "$teamName"
        team2Layout.isVisible = true
        playerAdapters[1].setData(players)
    }

    private fun updateTeam3(teamName: String, players: List<Player>) {
        team3Name.text = "$teamName"
        team3Layout.isVisible = true
        playerAdapters[2].setData(players)
    }

    private fun clear() {
        mCountDownTimer?.cancel()
        presenter.unBind()
    }

    private fun setupTimer() {
        updateTime(TURN_TIME_MILLIS)
    }

    private fun getFormattedTime(): String {
        val minutes = (mTimeLeftInMillis / 1000).toInt() / 60
        val seconds = (mTimeLeftInMillis / 1000).toInt() % 60

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onBackPressed(): Boolean {
        _emitter.onNext(UiEvent.OnBackPressed)
        return true
    }

    override fun onLogout(): Completable =
//        presenter.onLogout()
        Completable.complete()
}
