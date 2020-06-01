package com.gggames.celebs.presentation.gameon

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.celebs.R
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Player
import com.gggames.celebs.model.Team
import com.gggames.celebs.presentation.EndTurnDialogFragment
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent.RoundClick
import com.gggames.celebs.utils.showInfoToast
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_game_on.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject


/**
 * The main fragment in which the game is happening
 */
class GameOnFragment : Fragment(),
    GamePresenter.GameView {

    private lateinit var viewComponent: ViewComponent

    @Inject
    lateinit var presenter: GamePresenter

    private var mCountDownTimer: CountDownTimer? = null

    private var mTimeLeftInMillis = TURN_TIME_MILLIS

    private val _emitter = PublishSubject.create<UiEvent>()



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

        presenter.bind(this, _emitter)
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
            val buttonState = startButton.toButtonState()
            _emitter.onNext(UiEvent.StartStopClick(buttonState, mTimeLeftInMillis))
        }

        cardsAmountIcon.setOnClickListener {
            _emitter.onNext(UiEvent.CardsAmountClick)
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

    override fun setStartedState(meActive: Boolean, time:Long?) {
        time?.let {
            updateTime(time)
        }
        startTimer()
        startButton.text = "Pause"
        startButton.isEnabled = true
        endTurnButton.isEnabled = meActive
        correctButton.isEnabled = meActive

        val cardColor = if (meActive) {
            ContextCompat.getColor(requireContext(), R.color.green)
        } else {
            ContextCompat.getColor(requireContext(), R.color.gilad)
        }
        cardLayout.setBackgroundColor(cardColor)
    }


    override fun setStoppedState() {
        mCountDownTimer?.cancel()
        updateTime(TURN_TIME_MILLIS)
        startButton.text = "Start"
        correctButton.isEnabled = false
        endTurnButton.isEnabled = false
        startButton.isEnabled = true

        cardLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gilad))
    }

    override fun setCorrectEnabled(enabled: Boolean) {
        correctButton.isEnabled = enabled
    }

    var endTurnDialog : EndTurnDialogFragment? = null
    override fun showTurnEnded(player: Player?, cards: List<Card>) {
//        name?.let {
//            cardTextView.text = "$name's turn ended"
//        }
        player?.let {
            if (endTurnDialog?.isAdded != true) {
                endTurnDialog = EndTurnDialogFragment.create(player, cards)
                endTurnDialog?.show(requireActivity() as AppCompatActivity)
            }

        }
    }

    override fun showTurnEndedActivePlayer() {
        cardTextView.text = "Your turn ended"
    }

    override fun setPausedState(meActive: Boolean, time: Long?) {
        mCountDownTimer?.cancel()
        correctButton.isEnabled = false
        startButton.text = "Resume"
        startButton.isEnabled = meActive
        time?.let {
            updateTime(time)
        }
    }

    override fun setNewRound(meActive: Boolean, roundNumber: Int) {
        cardTextView.text = "Round $roundNumber is ready"
        startButton.isEnabled = meActive
        endTurnButton.isEnabled = false
    }

    private fun updateTime(time: Long) {
        mTimeLeftInMillis = time
        timerTextView?.text = getFormattedTime()
    }

    override fun setRoundEndState(meActive: Boolean, roundNumber: Int) {
        setPausedState(meActive, null)
        cardTextView.text = "Round $roundNumber ended"
        endTurnButton.isEnabled = false
        startButton.isEnabled = false
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
        team1Score.text = "(score: $score1) : "
        team2Score.text = "(score: $score2) : "
        if (score.size > 2) {
            val score3 = score[team3Name.text] ?: 0
            team3Score.text = "(score: $score3) : "
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
        cardTextView.text = "${player.name} is playing"
    }

    override fun setNoCurrentPlayer() {
        setStoppedState()
    }

    override fun setRound(round: String) {
        roundTextView.text = round
    }

    override fun showAllCards(cards: List<Card>) {
        val sb = java.lang.StringBuilder()
        cards.forEachIndexed { index, card ->
            sb.append("${index+1}: ${card.name}\n")
        }
        val dialogClickListener = DialogInterface.OnClickListener { _, _ ->
        }
        val builder = AlertDialog.Builder(context)
        builder
            .setTitle("All cards")
            .setMessage(sb.toString())
            .setPositiveButton(getString(R.string.ok), dialogClickListener)
            .show()
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
            _emitter.onNext(UiEvent.FinishGameClick)
        }
        cardLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gilad))
    }

    override fun navigateToGames() {
        findNavController().navigate(R.id.action_gameOnFragment_to_GamesFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
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

}

private fun Button.toButtonState(): GameScreenContract.ButtonState =
    when (this.text) {
        "Start" -> GameScreenContract.ButtonState.Stopped
        "Resume" -> GameScreenContract.ButtonState.Paused
        "Pause" -> GameScreenContract.ButtonState.Running
        else -> throw IllegalStateException("button state $this is unknown")
    }



