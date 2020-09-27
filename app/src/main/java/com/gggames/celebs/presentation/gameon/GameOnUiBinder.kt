package com.gggames.celebs.presentation.gameon

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gggames.celebs.R
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Player
import com.gggames.celebs.model.Team
import com.gggames.celebs.presentation.endturn.EndRoundDialogFragment
import com.gggames.celebs.presentation.endturn.EndTurnDialogFragment
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_game_on.view.*
import java.util.*
import javax.inject.Inject

class GameOnUiBinder @Inject constructor() {

    private lateinit var fragment: GameOnFragmentMVI

    private var view: View? = null

    private var context: Context? = null

    private var playerAdapters: List<PlayersAdapter> =
        listOf(PlayersAdapter(), PlayersAdapter(), PlayersAdapter())

    private lateinit var playersRecycleViews: List<RecyclerView>

    private var mCountDownTimer: CountDownTimer? = null

    var endRoundDialogFragment: EndRoundDialogFragment? = null
    var endTurnDialogFragment: EndTurnDialogFragment? = null

    private var mTimeLeftInMillis = TURN_TIME_MILLIS

    private val _emitter = PublishSubject.create<UiEvent>()
    val events: Observable<UiEvent> = _emitter

    private fun setup() {
        view?.apply {
            correctButton.setOnClickListener {
                it.isEnabled = false
                _emitter.onNext(UiEvent.CorrectClick(mTimeLeftInMillis))
            }

            roundTextView.setOnClickListener {
                _emitter.onNext(UiEvent.RoundClick(mTimeLeftInMillis))
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
        }
        setupTimer()
    }

    fun setFragment(fragment: GameOnFragmentMVI) {
        this.fragment = fragment
        view = fragment.activity?.window?.decorView
        context = view?.context
        setup()
    }

    fun render(state: GameScreenContract.State) {
        view?.apply {
            if (state.revealCurrentCard) {
                cardTextView?.text = state.currentCard?.name ?: ""
            } else {
                cardTextView?.text = state.currentPlayer?.name?.let { "$it is playing" } ?: ""
            }

            cardsAmount?.text = state.cardsInDeck.toString()
            setTeamNamesAndScore(state.teamsWithScore)
            setTeamPlayers(state.teamsWithPlayers)
            roundTextView?.text = state.round.roundNumber.toString()
            if (state.isTimerRunning && !state.inProgress) {
                startResumeTimer()
            } else {
                pauseTimer()
            }
            if (state.resetTime) {
                updateTime(TURN_TIME_MILLIS)
            }
            startButton?.state = state.playButtonState.state
            startButton?.isEnabled = state.playButtonState.isEnabled

            if (state.showEndOfTurn) {
                state.lastPlayer?.let { player ->
                    showEndTurn(player, state.cardsFoundInTurn, state.round.roundNumber)
                }
            }
            if (state.showEndOfRound) {
                showEndRound(state.previousRoundName, state.teamsWithScore)
            }
            if (state.showGameOver) {
                fragment.navigateToEndGame()
            }
            if (state.navigateToGames) {
                fragment.navigateToGames()
            }
            if (state.showLeaveGameConfirmation) {
                showLeaveGameDialog()
            }

            correctButton?.isEnabled = state.correctButtonEnabled && !state.inProgress
            helpButton?.isEnabled = state.helpButtonEnabled
        }
    }

    private fun startResumeTimer() {
        if (!isTimerRunning) {
            startTimer()
        }
    }

    private fun pauseTimer() {
        mCountDownTimer?.cancel()
        isTimerRunning = false
    }

    private fun setTeamPlayers(teams: List<Team>) {
        teams.forEachIndexed { index, team ->
            when (index) {
                0 -> updateTeam1(team.name, team.players)
                1 -> updateTeam2(team.name, team.players)
                2 -> updateTeam3(team.name, team.players)
            }
        }
    }

    private fun updateTeam1(teamName: String, players: List<Player>) {
        view?.team1Name?.text = "$teamName"
        view?.team1Layout?.isVisible = true
        playerAdapters[0].setData(players)
    }

    private fun updateTeam2(teamName: String, players: List<Player>) {
        view?.team2Name?.text = "$teamName"
        view?.team2Layout?.isVisible = true
        playerAdapters[1].setData(players)
    }

    private fun updateTeam3(teamName: String, players: List<Player>) {
        view?.team3Name?.text = "$teamName"
        view?.team3Layout?.isVisible = true
        playerAdapters[2].setData(players)
    }

    private fun setTeamNamesAndScore(teams: List<Team>) {
        if (teams.size > 2) {
            view?.team3Layout?.isVisible = true
        }
        teams.forEachIndexed { index, team ->
            when (index) {
                0 -> {
                    view?.team1Name?.text = team.name
                    view?.team1Name?.isSelected = true
                    view?.team1Score?.text = team.score.toString()
                }
                1 -> {
                    view?.team2Name?.text = team.name
                    view?.team2Name?.isSelected = true
                    view?.team2Score?.text = team.score.toString()
                }
                2 -> {
                    view?.team3Name?.text = team.name
                    view?.team3Name?.isSelected = true
                    view?.team3Score?.text = team.score.toString()
                }
            }
        }
    }

    var isTimerRunning = false
    private fun startTimer() {
        mCountDownTimer?.cancel()
        mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onFinish() {
                isTimerRunning = false
                mCountDownTimer = null
                _emitter.onNext(UiEvent.TimerEnd)
            }

            override fun onTick(millis: Long) {
                updateTime(millis)
            }
        }.start()
        isTimerRunning = true
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
            val builder = AlertDialog.Builder(context)
            builder.setMessage(ctx.getString(R.string.leave_game_dialog_title))
                .setPositiveButton(ctx.getString(R.string.ok), dialogClickListener)
                .setNegativeButton(ctx.getString(R.string.cancel), dialogClickListener)
                .show()
        }

    }

    private fun showEndRound(endedRoundName: String, teams: List<Team>) {
        if (endRoundDialogFragment?.isAdded != true) {
            endRoundDialogFragment = EndRoundDialogFragment.create(endedRoundName, teams)
            endRoundDialogFragment?.events()?.subscribe(_emitter)
            endRoundDialogFragment?.show(fragment.requireActivity() as AppCompatActivity)
        }
    }

    private fun showEndTurn(player: Player, cards: List<Card>, roundNumber: Int) {
        if (endTurnDialogFragment?.isAdded != true) {
            endTurnDialogFragment = EndTurnDialogFragment.create(player, cards, roundNumber)
            endTurnDialogFragment?.show(fragment.requireActivity() as AppCompatActivity)
        }
    }

    private fun setupTimer() {
        updateTime(TURN_TIME_MILLIS)
    }

    private fun updateTime(time: Long) {
        mTimeLeftInMillis = time
        view?.timerTextView?.text = getFormattedTime()
    }

    private fun getFormattedTime(): String {
        val minutes = (mTimeLeftInMillis / 1000).toInt() / 60
        val seconds = (mTimeLeftInMillis / 1000).toInt() % 60

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}