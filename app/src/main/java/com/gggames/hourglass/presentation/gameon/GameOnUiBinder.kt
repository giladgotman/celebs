package com.gggames.hourglass.presentation.gameon

import android.content.Context
import android.content.DialogInterface
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gggames.hourglass.R
import com.gggames.hourglass.model.*
import com.gggames.hourglass.presentation.MainActivity
import com.gggames.hourglass.presentation.cardunknown.CardInfoFragment
import com.gggames.hourglass.presentation.endround.ChangeRoundDialogFragment
import com.gggames.hourglass.presentation.endround.WelcomeFirstRoundFragment
import com.gggames.hourglass.presentation.endturn.EndTurnDialogFragment
import com.gggames.hourglass.presentation.gameon.GameScreenContract.UiEvent
import com.gggames.hourglass.utils.RxTimer
import com.gggames.hourglass.utils.TimerEvent
import com.gggames.hourglass.utils.createToolTip
import com.gggames.hourglass.utils.media.AudioPlayer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.BalloonAnimation
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_game_on.view.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GameOnUiBinder @Inject constructor(
    val schedulerProvider: BaseSchedulerProvider,
    val audioPlayer: AudioPlayer
) {

    private lateinit var fragment: GameOnFragmentMVI

    private var view: View? = null

    private var context: Context? = null

    private val teamsAdapter = TeamsAdapter()

    private val rxTimer = RxTimer(schedulerProvider)

    private var endRoundDialogFragment: ChangeRoundDialogFragment? = null
    private var endTurnDialogFragment: EndTurnDialogFragment? = null
    private var cardInfoFragment: CardInfoFragment? = null

    private var isEndTurnEnabled = false

    private val _emitter = PublishSubject.create<UiEvent>()
    val events: Observable<UiEvent> = _emitter

    private val disposables = CompositeDisposable()

    private fun setup() {
        view?.apply {
            correctButton.setOnClickListener {
                _emitter.onNext(UiEvent.CorrectClick(rxTimer.time))
            }

            roundTextView.setOnClickListener {
                _emitter.onNext(UiEvent.RoundClick(rxTimer.time))
            }

            startButton.setOnClickListener {
                _emitter.onNext(UiEvent.StartStopClick(startButton.state, rxTimer.time))
            }

            cardsAmount.setOnClickListener {
                _emitter.onNext(UiEvent.CardsAmountClick)
            }

            helpButton.setOnClickListener {
                _emitter.onNext(UiEvent.CardInfoClick(cardTextView.text.toString()))
            }

            teamsView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            teamsView.adapter = teamsAdapter
        }
        rxTimer.observe()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                Timber.v("ttt TIMER EVENT $it")
                when (it) {
                    is TimerEvent.UpdatedTime -> view?.hourglass?.state = view!!.hourglass.state.copy(time = it.time)
                    is TimerEvent.TimerEnd -> _emitter.onNext(UiEvent.TimerEnd)
                    is TimerEvent.Tick -> {
                        if (it.time == 10000L) {
                            audioPlayer.play("clock_ticking")
                        }

                    }
                }
            }.let { disposables.add(it) }
        rxTimer.time = TURN_TIME_MILLIS
    }

    fun setFragment(fragment: GameOnFragmentMVI) {
        this.fragment = fragment
        view = fragment.activity?.window?.decorView
        context = view?.context
        setup()
    }

    fun render(state: GameScreenContract.State) {
        val isFragmentVisible = fragment.isResumed
        view?.apply {
            (fragment.activity as MainActivity).setTitle(state.screenTitle)

            if (state.revealCurrentCard) {
                cardTextView?.text = state.currentCard?.name ?: ""
            } else {
                cardTextView?.text = state.currentPlayer?.name?.let { "$it is playing" } ?: ""
            }

            cardsAmount?.text = state.cardsInDeck.toString()

            val teamsDataSet = state.teamsWithScore.map { teamWithScore ->
                val team = state.teamsWithPlayers.find { it.name == teamWithScore.name } ?: TeamWithPlayers()
                team.let {
                    TeamsAdapter.TeamItem(
                        teamWithScore.name,
                        PlayersDataSet(team.players, state.currentPlayer, state.nextPlayer),
                        teamWithScore.score
                    )
                }
            }
            teamsAdapter.setData(teamsDataSet)

            roundTextView?.text = state.round.roundNumber.toString()

            // Time
            if (state.useLocalTimer) {
                if (state.isTimerRunning && !state.inProgress) {
                    resumeTimer()
                } else {
                    pauseTimer()
                }
            }
            if (state.round.turn.state == TurnState.Over) {
                rxTimer.stop()
            }

            if (state.resetTime) {
                rxTimer.time = TURN_TIME_MILLIS
            }

            if (!state.meActive) {
                state.time?.let { rxTimer.updateTime(it) }
            }

            // Buttons
            startButton?.state = state.playButtonState.state
            startButton?.isEnabled = state.playButtonState.isEnabled
            cardsAmount?.isEnabled = state.isCardsAmountEnabled
            correctButton?.isEnabled = state.correctButtonEnabled && !state.inProgress
            helpButton?.isEnabled = state.helpButtonEnabled

            // Dialogs
            if (state.showEndOfTurn) {
                cardInfoFragment?.let { if (it.isResumed) it.dismiss() }
                state.lastPlayer?.let { player ->
                    if (isFragmentVisible) {
                        showEndTurn(player, state.nextPlayer, state.cardsFoundInTurn, state.round.roundNumber)
                    }
                }
            }
            if (state.showEndOfRound) {
                cardInfoFragment?.let { if (it.isResumed) it.dismiss() }
                if (isFragmentVisible) {
                    showEndRound(state.round, state.teamsWithScore)
                }
            }
            if (state.showRoundInstructions) {
                showFirstRoundIntro(state.round, state.nextPlayer)
            }
            if (state.showLeaveGameConfirmation) {
                showLeaveGameDialog()
            }
            if (state.showEndTurnConfirmation) {
                showEndTurnDialog()
            }

            // Navigation
            if (state.showGameOver) {
                cardInfoFragment?.let { if (it.isResumed) it.dismiss() }
                fragment.navigateToEndGame()
            }
            if (state.navigateToGames) {
                fragment.navigateToGames()
            }
            if (state.navigateToTeams) {
                fragment.navigateToTeams()
            }

            // Toolbar
            if (isEndTurnEnabled != state.isEndTurnEnabled) {
                isEndTurnEnabled = state.isEndTurnEnabled
                fragment.activity?.invalidateOptionsMenu()
            }

            // Tooltip
            if (state.showPlayTooltip) {
                Observable.timer(1, TimeUnit.SECONDS).subscribe {
                    showPlayTooltip(startButton)
                }
            }

            view?.hourglass?.state = view!!.hourglass.state.copy(turnState = state.round.turn.state)
        }
    }

    private fun showCardInfo(cardName: String) {
        cardInfoFragment?.let {
            it.dismiss()
        }
        cardInfoFragment = CardInfoFragment.newInstance(cardName)
        cardInfoFragment!!.show(fragment.requireActivity() as AppCompatActivity)
    }

    fun trigger(trigger: GameScreenContract.Trigger) {
        when (trigger) {
            is GameScreenContract.Trigger.ShowAllCards -> showAllCards(trigger.cards)
            is GameScreenContract.Trigger.StartTimer -> rxTimer.start()
            is GameScreenContract.Trigger.ShowCardInfo -> {
                rxTimer.updateTime(rxTimer.time - 10000)
                showCardInfo(trigger.cardName)
            }
        }
    }

    private fun resumeTimer() {
        rxTimer.resume()
    }

    private fun pauseTimer() {
        rxTimer.pause()
    }

    private fun showLeaveGameDialog() {
        context?.let { ctx ->
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        _emitter.onNext(UiEvent.UserApprovedQuitGame)
                    }

                    DialogInterface.BUTTON_NEGATIVE -> {
                    }
                }
            }
            val builder = MaterialAlertDialogBuilder(ctx, R.style.celebs_MaterialAlertDialog)
            builder.setMessage(ctx.getString(R.string.leave_game_dialog_title))
                .setPositiveButton(ctx.getString(R.string.ok), dialogClickListener)
                .setNegativeButton(ctx.getString(R.string.cancel), dialogClickListener)
                .show()
        }

    }

    private fun showEndTurnDialog() {
        context?.let { ctx ->
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        _emitter.onNext(UiEvent.UserApprovedEndTurn)
                    }

                    DialogInterface.BUTTON_NEGATIVE -> {
                    }
                }
            }
            val builder = MaterialAlertDialogBuilder(ctx, R.style.celebs_MaterialAlertDialog)
            builder.setMessage(ctx.getString(R.string.end_turn_dialog_title))
                .setPositiveButton(ctx.getString(R.string.ok), dialogClickListener)
                .setNegativeButton(ctx.getString(R.string.cancel), dialogClickListener)
                .show()
        }

    }

    private fun showEndRound(currRound: Round, teams: List<Team>) {
        if (endRoundDialogFragment == null) {
            endRoundDialogFragment = ChangeRoundDialogFragment.newInstance(currRound, true, teams)
            endRoundDialogFragment?.show(fragment.requireActivity() as AppCompatActivity)
            endRoundDialogFragment?.setOnDismiss { endRoundDialogFragment = null }
        }
    }

    private fun showAllCards(cards: List<Card>) {
        context?.let { ctx ->
            val sb = java.lang.StringBuilder()
            cards.forEachIndexed { index, card ->
                sb.append("${index + 1}: ${card.name}\n")
            }
            val dialogClickListener = DialogInterface.OnClickListener { _, _ ->
            }
            val builder = MaterialAlertDialogBuilder(ctx, R.style.celebs_MaterialAlertDialog)
            builder
                .setTitle("All cards")
                .setMessage(sb.toString())
                .setPositiveButton(ctx.getString(R.string.ok), dialogClickListener)
                .show()
        }

    }

    private fun showFirstRoundIntro(round: Round, nextPlayer: Player?) {
        val welcomeFrag =
            WelcomeFirstRoundFragment.newInstance(round.roundNumber, roundIdToName(round.roundNumber), nextPlayer)
        welcomeFrag.show(fragment.requireActivity() as AppCompatActivity)

        welcomeFrag.events().subscribe(_emitter::onNext).let { disposables.addAll(it) }
    }

    private fun showEndTurn(player: Player, nextPlayer: Player?, cards: List<Card>, roundNumber: Int) {
        if (endTurnDialogFragment?.isAdded != true) {
            endTurnDialogFragment = EndTurnDialogFragment.create(player, nextPlayer, cards, roundNumber)
            endTurnDialogFragment?.show(fragment.requireActivity() as AppCompatActivity)
        }
    }

    private fun getFormattedTime(rawTime: Long): String {
        val minutes = (rawTime / 1000).toInt() / 60
        val seconds = (rawTime / 1000).toInt() % 60

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_end_turn).isVisible = isEndTurnEnabled
        return true
    }

    fun clear() {
        disposables.clear()
    }

    private fun showPlayTooltip(view: View) {
        context?.let { ctx ->
            val tooltip = createToolTip(
                ctx,
                ArrowOrientation.BOTTOM,
                "Press here to start your turn",
                lifecycleOwner = fragment.viewLifecycleOwner,
                animation = BalloonAnimation.FADE,
                countLabel = "startButton"
            )
            tooltip.setOnBalloonClickListener { tooltip.dismiss() }
            tooltip.showAlignBottom(view)
        }
    }
}