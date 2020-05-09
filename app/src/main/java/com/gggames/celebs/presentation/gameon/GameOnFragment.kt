package com.gggames.celebs.presentation.gameon

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.celebs.R
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Player
import com.gggames.celebs.model.Team
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import com.gggames.celebs.utils.showInfoToast
import kotlinx.android.synthetic.main.fragment_game_on.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject


/**
 * The main fragment in which the game is happening
 */
class GameOnFragment : Fragment(),
    GamePresenter.GameView {

    private val START_TIME_IN_MILLIS = 60000L
//    private val START_TIME_IN_MILLIS = 20000L

    private lateinit var viewComponent: ViewComponent

    @Inject
    lateinit var presenter: GamePresenter

    private var mCountDownTimer: CountDownTimer? = null

    private var mTimeLeftInMillis = START_TIME_IN_MILLIS


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_on, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewComponent = createViewComponent(this)
        viewComponent.inject(this)

        presenter.bind(this)
        cardTextView.text = ""

        endTurnButton.setOnClickListener {
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        timerTextView.text = "Turn Ended"
                        presenter.onTurnEnded()
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
            presenter.onCorrectClick()
        }

        roundTextView.setOnClickListener {
            presenter.onNewRoundClick()
        }

        startButton.setOnClickListener {
            val text = startButton.text.toString()
            val buttonState = text.toButtonState()
            presenter.onStartButtonClick(buttonState)

        }
        hideTeamsInfo()
        setStoppedState()
        setupTimer()
    }

    private fun hideTeamsInfo() {
        team1Layout.isVisible = false
        team2Layout.isVisible = false
        team3Layout.isVisible = false
    }

    override fun showNewRoundAlert(onClick: (Boolean) -> Unit) {
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    onClick(true)
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                    onClick(false)
                }
            }
        }
        val builder = AlertDialog.Builder(context)
        builder.setMessage(getString(R.string.new_round_alert_message))
            .setPositiveButton(getString(R.string.ok), dialogClickListener)
            .setNegativeButton(getString(R.string.cancel), dialogClickListener)
            .show()
    }

    override fun showLastRoundToast() {
        showInfoToast(requireContext(), "This is the last round", Toast.LENGTH_LONG)
    }

    override fun setStartedState() {
        startTimer()
        startButton.text = "Pause"
        startButton.isEnabled = true
        endTurnButton.isEnabled = true
        correctButton.isEnabled = true
    }


    override fun setStoppedState() {
        mTimeLeftInMillis = START_TIME_IN_MILLIS
        startButton.text = "Start"
        cardTextView.text = ""
        correctButton.isEnabled = false
        endTurnButton.isEnabled = false
        startButton.isEnabled = true
    }

    override fun setPausedState() {
        mCountDownTimer?.cancel()
        correctButton.isEnabled = false
        startButton.text = "Resume"
        startButton.isEnabled = true
    }

    override fun setRoundEndState() {
        setPausedState()
        cardTextView.text = "Round Ended"
        startButton.isEnabled = false
    }

    private fun startTimer() {
        mCountDownTimer?.cancel()
        mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onFinish() {
                timerTextView.text = "Time's Up!"
                presenter.onTurnEnded()
            }

            override fun onTick(millis: Long) {
                mTimeLeftInMillis = millis
                updateCountDownText()
            }
        }.start()
    }

    override fun updateCards(cards: List<Card>) {
        cardsAmount.text = cards.size.toString()
    }

    override fun updateCard(card: Card) {
        Timber.w("ggg update card: $card")
        cardTextView.text = card.name
    }

    override fun updateTeams(teams: List<Team>) {
        teams.forEachIndexed { index, team ->
            when (index) {
                0 -> updateTeam1(team.name, team.players)
                1 -> updateTeam2(team.name, team.players)
                2 -> updateTeam3(team.name, team.players)
            }
        }
    }

    private fun updateTeam1(teamName: String, players: List<Player>) {
        team1Name.text = "$teamName"
        team1Layout.isVisible = true
        setPlayersForTeam(team1Value, players)
    }

    private fun updateTeam2(teamName: String, players: List<Player>) {
        team2Name.text = "$teamName"
        team2Layout.isVisible = true
        setPlayersForTeam(team2Value, players)
    }

    private fun updateTeam3(teamName: String, players: List<Player>) {
        team3Name.text = "$teamName"
        team3Layout.isVisible = true
        setPlayersForTeam(team3Value, players)
    }

    private fun setPlayersForTeam(teamValue: TextView, players: List<Player>) {
        val sb = StringBuilder()
        players.forEachIndexed { i, player ->
            sb.append(player.name)
            if (i < players.lastIndex) {
                sb.append(", ")
            }
        }
        teamValue.text = sb.toString()
    }

    override fun setScore(score: Map<String, Int>) {
        val score1 = score[team1Name.text] ?: 0
        val score2 = score[team2Name.text] ?: 0
        team1Score.text = "($score1) : "
        team2Score.text = "($score2) : "
        if (score.size > 2) {
            val score3 = score[team3Name.text] ?: 0
            team3Score.text = "($score3) : "
        }
    }

    override fun setTeamNames(teams: List<Team>) {
        teams.forEachIndexed { index, team ->
            when (index) {
                0 -> team1Name.text = team.name
                1 -> team2Name.text = team.name
                2 -> team3Name.text = team.name
            }
        }
    }

    override fun setCurrentOtherPlayer(player: Player) {
        startButton.isEnabled = false
        cardTextView.text = "${player.name} is playing"
    }

    override fun setNoCurrentPlayer() {
        setStoppedState()
    }

    override fun setRound(round: String) {
        roundTextView.text = round
    }

    override fun showGameOver() {
        cardTextView.text = "Game Over!"
        timerTextView?.text = ""
        mCountDownTimer?.cancel()
        startButton.text = "FINISH"
        startButton.isEnabled = true
        correctButton.isEnabled = false
        endTurnButton.isEnabled = false
        startButton.setOnClickListener {
            findNavController().navigate(R.id.action_gameOnFragment_to_GamesFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountDownTimer?.cancel()
        presenter.unBind()
    }

    private fun setupTimer() {
        updateCountDownText()
    }

    private fun updateCountDownText() {
        val minutes = (mTimeLeftInMillis / 1000).toInt() / 60
        val seconds = (mTimeLeftInMillis / 1000).toInt() % 60

        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        timerTextView?.text = timeLeftFormatted
    }

}

private fun String.toButtonState(): ButtonState =
    when (this) {
        "Start" -> ButtonState.Stopped
        "Resume" -> ButtonState.Paused
        "Pause" -> ButtonState.Running
        else -> throw IllegalStateException("button state $this is unknown")
    }


enum class ButtonState {
    Stopped,
    Running,
    Paused
}
