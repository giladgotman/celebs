package com.gggames.celebs.presentation

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private val START_TIME_IN_MILLIS = 60000L
//    private val START_TIME_IN_MILLIS = 20000L

    lateinit var presenter: GamePresenter

    private var mCountDownTimer: CountDownTimer? = null

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
            pickNextCard()
        }

        roundTextView.setOnClickListener {
            presenter.onNewRoundClick()
        }

        startButton.setOnClickListener {
            presenter.onStartButtonClick()

        }
        setStoppedState()
        setupTimer()
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
        Toast.makeText(this.context, "This is the last round", Toast.LENGTH_LONG).show()
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
        cardTextView.text = "${player.name} is playing"
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
        timerTextView?.text = ""
        mCountDownTimer?.cancel()
        startButton.text = "FINISH"
        startButton.isEnabled = true
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

        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        timerTextView?.text = timeLeftFormatted;
    }

}
