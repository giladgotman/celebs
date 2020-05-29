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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.celebs.R
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Player
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent.RoundClick
import com.gggames.celebs.utils.showInfoToast
import io.reactivex.disposables.CompositeDisposable
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

    private lateinit var teamNameTextViews: List<TextView>
    private lateinit var teamLayouts: List<ConstraintLayout>
    private lateinit var teamMembersTextViews: List<TextView>
    private lateinit var teamScoreTextViews: List<TextView>

    private val disposables = CompositeDisposable()

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


        cardTextView.text = ""

        teamNameTextViews = mutableListOf(team1Name, team2Name, team3Name)
        teamLayouts = mutableListOf(team1Layout, team2Layout, team3Layout)
        teamMembersTextViews = mutableListOf(team1Value, team2Value, team3Value)
        teamScoreTextViews = mutableListOf(team1Score, team2Score, team3Score)

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


        presenter.teamState.subscribe{
            renderTeams(it)
        }.let { disposables.add(it) }

        presenter.bind(this, _emitter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
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

    override fun showTurnEnded(name: String?) {
        name?.let {
            cardTextView.text = "$name's turn ended"
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

    fun renderTeams(teamsState: GameScreenContract.TeamsState) {
        teamsState.teamsList.forEachIndexed { index, team ->
            renderTeam(index, team)
        }
    }

    private fun renderTeam(index: Int, team: GameScreenContract.TeamState) {
        teamNameTextViews[index].text = team.name
        teamLayouts[index].isVisible = true
        renderTeamMembers(index, team.players)
        renderTeamScore(index, team.score)
    }

    private fun renderTeamScore(index: Int, score: Int) {
        teamScoreTextViews[index].text = "($score) : "
    }

    private fun renderTeamMembers(index: Int, players: List<String>) {
        val sb = StringBuilder()
        players.forEachIndexed { i, playerNamr ->
            sb.append(playerNamr)
            if (i < players.lastIndex) {
                sb.append(", ")
            }
        }
        teamMembersTextViews[index].text = sb.toString()
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



