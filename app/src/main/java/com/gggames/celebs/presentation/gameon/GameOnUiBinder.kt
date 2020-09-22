package com.gggames.celebs.presentation.gameon

import android.content.Context
import android.os.CountDownTimer
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gggames.celebs.model.Player
import com.gggames.celebs.model.Team
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

    private var mTimeLeftInMillis = TURN_TIME_MILLIS

    private val _emitter = PublishSubject.create<GameScreenContract.UiEvent>()
    val events = _emitter

    private fun setup() {
        view?.apply {
            correctButton.setOnClickListener {
                _emitter.onNext(GameScreenContract.UiEvent.CorrectClick(mTimeLeftInMillis))
            }

            roundTextView.setOnClickListener {
                _emitter.onNext(GameScreenContract.UiEvent.RoundClick(mTimeLeftInMillis))
            }

            startButton.setOnClickListener {
                _emitter.onNext(GameScreenContract.UiEvent.StartStopClick(startButton.state, mTimeLeftInMillis))
            }

            cardsAmount.setOnClickListener {
                _emitter.onNext(GameScreenContract.UiEvent.CardsAmountClick)
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

    fun render(state: GameScreenContract.State) {
        view?.apply {
            cardTextView?.text = state.currentCard?.name ?: ""
            cardsAmount?.text = state.cardsInDeck.toString()
            setTeams(state.teams)
            updateTeams(state.teams)
            roundTextView.text = state.round
            if (state.isTimerRunning) {
                if (mCountDownTimer == null) {
                    startTimer()
                }
            } else {
                mCountDownTimer?.cancel()
            }
        }

    }

    fun setFragment(fragment: GameOnFragmentMVI) {
        this.fragment = fragment
        view = fragment.activity?.window?.decorView
        context = view?.context
        setup()
    }


    private fun updateTeams(teams: List<Team>) {
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

    fun setTeams(teams: List<Team>) {
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

    private fun startTimer() {
        mCountDownTimer?.cancel()
        mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onFinish() {
                mCountDownTimer = null
                _emitter.onNext(GameScreenContract.UiEvent.TimerEnd)
            }

            override fun onTick(millis: Long) {
                updateTime(millis)
            }
        }.start()
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