package com.gggames.hourglass.presentation.gameon

import android.content.Context
import android.content.DialogInterface
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gggames.hourglass.R
import com.gggames.hourglass.model.*
import com.gggames.hourglass.presentation.MainActivity
import com.gggames.hourglass.presentation.endturn.ChangeRoundDialogFragment
import com.gggames.hourglass.presentation.endturn.EndTurnDialogFragment
import com.gggames.hourglass.presentation.endturn.WelcomeFirstRoundFragment
import com.gggames.hourglass.presentation.gameon.GameScreenContract.UiEvent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    var endRoundDialogFragment: ChangeRoundDialogFragment? = null
    var endTurnDialogFragment: EndTurnDialogFragment? = null

    private var mTimeLeftInMillis = TURN_TIME_MILLIS

    private val _emitter = PublishSubject.create<UiEvent>()
    val events: Observable<UiEvent> = _emitter

    private fun setup() {
        view?.apply {
            correctButton.setOnClickListener {
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
        val isFragmentVisible = fragment.isResumed
        view?.apply {
            (fragment.activity as MainActivity).setTitle(state.screenTitle)

            if (state.revealCurrentCard) {
                cardTextView?.text = state.currentCard?.name ?: ""
            } else {
                cardTextView?.text = state.currentPlayer?.name?.let { "$it is playing" } ?: ""
            }

            cardsAmount?.text = state.cardsInDeck.toString()
            setTeamNamesAndScore(state.teamsWithScore)
            val updatedTeams =
                updateTeamsWithPlayingState(state.teamsWithPlayers, state.currentPlayer, state.nextPlayer)
            setTeamPlayers(updatedTeams)
            roundTextView?.text = state.round.roundNumber.toString()
            if (state.useLocalTimer) {
                if (state.isTimerRunning && !state.inProgress) {
                    startResumeTimer()
                } else {
                    pauseTimer()
                }
            }
            if (state.resetTime) {
                updateTime(TURN_TIME_MILLIS)
            }
            startButton?.state = state.playButtonState.state
            startButton?.isEnabled = state.playButtonState.isEnabled

            if (state.showEndOfTurn) {
                state.lastPlayer?.let { player ->
                    if (isFragmentVisible) {
                        showEndTurn(player, state.cardsFoundInTurn, state.round.roundNumber)
                    }
                }
            }
            if (state.showEndOfRound) {
                if (isFragmentVisible) {
                    showEndRound(state.round, state.teamsWithScore)
                }
            }

            if (state.showRoundInstructions) {
                showFirstRoundIntro(state.round, state.nextPlayer)
            }
            if (state.showGameOver) {
                fragment.navigateToEndGame()
            }
            if (state.navigateToGames) {
                fragment.navigateToGames()
            }

            if (state.navigateToTeams) {
                fragment.navigateToTeams()
            }
            if (state.showLeaveGameConfirmation) {
                showLeaveGameDialog()
            }

            state.time?.let { updateTime(it) }

            correctButton?.isEnabled = state.correctButtonEnabled && !state.inProgress
            helpButton?.isEnabled = state.helpButtonEnabled
        }
    }

    // update the player.playerTurnState in each team based on the currentPlayer
    private fun updateTeamsWithPlayingState(
        teamsWithPlayers: List<Team>,
        currentPlayer: Player?,
        nextPlayer: Player?
    ): List<Team> {
        val teams = mutableListOf<Team>()
        teamsWithPlayers.forEachIndexed { index, team ->
            val players = mutableListOf<Player>()
            team.players.forEachIndexed { pIndex, player ->
                val playerTurnState = when {
                    currentPlayer?.id == player.id -> {
                        PlayerTurnState.Playing
                    }
                    nextPlayer?.id == player.id -> {
                        PlayerTurnState.UpNext
                    }
                    else -> {
                        PlayerTurnState.Idle
                    }
                }
                players.add(teamsWithPlayers[index].players[pIndex].copy(playerTurnState = playerTurnState))

            }
            teams.add(team.copy(players = players))
        }
        return teams
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
        view?.team1Name?.text = teamName
        view?.team1Layout?.isVisible = true
        playerAdapters[0].setData(players)
    }

    private fun updateTeam2(teamName: String, players: List<Player>) {
        view?.team2Name?.text = teamName
        view?.team2Layout?.isVisible = true
        playerAdapters[1].setData(players)
    }

    private fun updateTeam3(teamName: String, players: List<Player>) {
        view?.team3Name?.text = teamName
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
            val builder = MaterialAlertDialogBuilder(ctx, R.style.celebs_MaterialAlertDialog)
            builder.setMessage(ctx.getString(R.string.leave_game_dialog_title))
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

    private fun showFirstRoundIntro(round: Round, nextPlayer: Player?) {
        val welcomeFrag =
            WelcomeFirstRoundFragment.newInstance(round.roundNumber, roundIdToName(round.roundNumber), nextPlayer)
        welcomeFrag.show(fragment.requireActivity() as AppCompatActivity)
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