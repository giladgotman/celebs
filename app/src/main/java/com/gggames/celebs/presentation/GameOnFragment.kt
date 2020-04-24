package com.gggames.celebs.presentation

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.celebs.R
import com.gggames.celebs.data.model.Card
import com.gggames.celebs.data.model.Player
import kotlinx.android.synthetic.main.fragment_game_on.*
import timber.log.Timber
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class GameOnFragment : Fragment(), GamePresenter.GameView {

    val STATE_STOPPED = 0
    val STATE_STARTED = 1
    val STATE_PAUSED = 2
    val STATE_NEW_ROUND = 3

    private var state: Int = STATE_STOPPED

    //    private val START_TIME_IN_MILLIS = 60000L
    private val START_TIME_IN_MILLIS = 30000L

    lateinit var presenter: GamePresenter


    private var mCountDownTimer: CountDownTimer? = null

    private var mTimerRunning = false

    private var mTimeLeftInMillis = START_TIME_IN_MILLIS


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        presenter = GamePresenter()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_on, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter.bind(this)
        cardTextView.text = ""

        resetButton.setOnClickListener {
            resetTimer()
        }


        correctButton.setOnClickListener {
            pickNextCard()
        }

        roundTextView.setOnClickListener {
            presenter.onReloadDeck()
        }

        startButton.setOnClickListener {
            when (state) {
                STATE_STOPPED -> presenter.onPlayerStarted()
                STATE_PAUSED -> presenter.onPlayerResumed()
                STATE_STARTED -> presenter.onPlayerPaused()
                STATE_NEW_ROUND -> presenter.onPlayerResumedNewRound()
            }
        }
        setStoppedState()
        setupTimer()
    }

    override fun setStartedState() {
        startTimer()
        mTimerRunning = true
        state = STATE_STARTED
        startButton.text = "Pause"
        startButton.isEnabled = true
        resetButton.isEnabled = true
        correctButton.isEnabled = true
    }



    override fun setStoppedState() {
        state = STATE_STOPPED
        mTimeLeftInMillis = START_TIME_IN_MILLIS
        startButton.text = "Start"
        cardTextView.text = ""
        correctButton.isEnabled = false
        startButton.isEnabled = true
    }

    override fun setPausedState() {
        state = STATE_PAUSED
        mCountDownTimer?.cancel()
        correctButton.isEnabled = false
        mTimerRunning = false
        startButton.text = "Resume"
        startButton.isEnabled = true
    }

    override fun setRoundEndState() {
        state = STATE_NEW_ROUND
        mCountDownTimer?.cancel()
        correctButton.isEnabled = false
        mTimerRunning = false
        startButton.text = "Resume"
        startButton.isEnabled = true
    }

    private fun startTimer() {
        mCountDownTimer?.cancel()
        mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onFinish() {
                mTimerRunning = false
                timerTextView.text = "Time's Up!"
                presenter.onTurnEnded()
            }

            override fun onTick(millis: Long) {
                mTimeLeftInMillis = millis
                updateCountDownText()
            }
        }.start()
    }
    private fun pickNextCard() {
        presenter.onPickNextCard()
    }

    override fun updateCards(cards: List<Card>) {
        cardsAmount.text = cards.size.toString()
    }

    override fun updateCard(card: Card) {
        Timber.w("ggg update card: $card")
        cardTextView.text = card.name
    }

    override fun showNoCardsLeft() {
        setPausedState()
        cardTextView.text = "Round Ended"
        startButton.isEnabled = false
        startButton.text = "---"
        resetButton.isEnabled = false
    }

    override fun updateTeams(list: List<Player>) {
        val teams = list.groupBy { it.team }
        val teamList = teams.keys.toList().filterNotNull()
        teamList.forEachIndexed { index, teamName ->
            val players = teams[teamName]?.toList() ?: emptyList()
            when (index) {
                0 -> updateTeam1(teamName, players)
                1 -> updateTeam2(teamName, players)
            }
        }
    }

    private fun updateTeam1(teamName: String, players: List<Player>) {
        Timber.w("updateTeam : teamName: $teamName , p: ${players.size}")
        team1Name.text = "$teamName : "
        val sb = StringBuilder()
        players.forEach {
            sb.append(it.name)
            sb.append(", ")
        }
        team1Value.text = sb.toString()
    }

    private fun updateTeam2(teamName: String, players: List<Player>) {
        Timber.w("updateTeam2 : teamName: $teamName , p: ${players.size}")
        team2Name.text = "$teamName : "
        val sb = StringBuilder()
        players.forEach {
            sb.append(it.name)
            sb.append(", ")
        }
        team2Value.text = sb.toString()
    }

    override fun setCurrentOtherPlayer(player: Player) {
        startButton.isEnabled = false
        cardTextView.text = "${player.name}'s turn"
    }

    override fun setNoCurrentPlayer() {
        setStoppedState()
    }

    override fun setRound(round: String) {
        roundTextView.text = round
    }

    //todo add update Team3

    override fun showGameOver() {
        cardTextView.text = "Game Over!"
        startButton.text = "FINISH"
        startButton.isEnabled = true
        startButton.setOnClickListener {
            findNavController().navigate(R.id.action_gameOnFragment_to_GamesFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.unBind()
    }

    private fun setupTimer() {
        updateCountDownText()
    }

    private fun resetTimer() {
        mCountDownTimer?.cancel()
        mTimerRunning = false
        startButton.text = "Start"
        mTimeLeftInMillis = START_TIME_IN_MILLIS
        updateCountDownText()
    }

    private fun updateCountDownText() {
        val minutes = (mTimeLeftInMillis / 1000).toInt() / 60
        val seconds = (mTimeLeftInMillis / 1000).toInt() % 60

        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        timerTextView.text = timeLeftFormatted;
    }

}
