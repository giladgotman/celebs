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
class GameOnFragment : Fragment(),
    GamePresenter.GameView, MainActivityDelegate {

    private lateinit var viewComponent: ViewComponent

    @Inject
    lateinit var presenter: GamePresenter

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

        helpButton.setOnClickListener {
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
        presenter.bind(this, uiEvents)
    }

    override fun onStop() {
        super.onStop()
        clear()
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

    override fun setStartedState(meActive: Boolean, time: Long?) {
        time?.let {
            updateTime(time)
        }
        startTimer()
        startButton.state = ButtonState.Running
        startButton.isEnabled = true
        helpButton.isEnabled = false

        val cardColor = if (meActive) {
            ContextCompat.getColor(requireContext(), R.color.green)
        } else {
            ContextCompat.getColor(requireContext(), R.color.gilad)
        }
//        cardLayout.setBackgroundColor(cardColor)
    }

    override fun setTurnStoppedState() {
        mCountDownTimer?.cancel()
        updateTime(TURN_TIME_MILLIS)
        startButton.state = ButtonState.Stopped
        correctButton.isEnabled = false
        helpButton.isEnabled = false
        startButton.isEnabled = true
    }

    override fun setCorrectEnabled(enabled: Boolean) {
        correctButton.isEnabled = enabled
    }

    override fun showTurnEnded(player: Player?, cards: List<Card>, roundNumber: Int) {
        player?.let {
            cardTextView.text = ""
            findNavController().navigate(
                R.id.action_gameOnFragment_to_endTurnDialogFragment,
                Bundle().apply {
                    putString(KEY_PLAYER_NAME, player.name)
                    putParcelableArray(KEY_CARDS, cards.toTypedArray())
                    putInt(KEY_ROUND_NUMBER, roundNumber)
                }
            )
        }
    }

    var endRoundDialogFragment: EndRoundDialogFragment? = null
    override fun showRoundEnded(round: Round, teams: List<Team>) {
            cardTextView.text = ""
            if (endRoundDialogFragment?.isAdded != true) {
                endRoundDialogFragment = EndRoundDialogFragment.create(round, teams)
                endRoundDialogFragment?.show(requireActivity() as AppCompatActivity)
            }
    }

    override fun showTurnEndedActivePlayer() {
        cardTextView.text = "Your turn ended"
    }

    override fun setPausedState(playButtonEnabled: Boolean, time: Long?) {
        mCountDownTimer?.cancel()
        correctButton.isEnabled = false
        startButton.state = ButtonState.Paused
        startButton.isEnabled = playButtonEnabled
        time?.let {
            updateTime(time)
        }
    }

    override fun setNewRound(playButtonEnabled: Boolean, roundNumber: Int) {
        cardTextView.text = "Round $roundNumber is ready"
        startButton.isEnabled = playButtonEnabled
        helpButton.isEnabled = false
    }

    private fun updateTime(time: Long) {
        mTimeLeftInMillis = time
        timerTextView?.text = getFormattedTime()
    }

    override fun setRoundEndState(meActive: Boolean, roundNumber: Int) {
        setPausedState(meActive, null)
        helpButton.isEnabled = false
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

    override fun setTeams(teams: List<Team>) {
        if (teams.size > 2) {
            team3Layout.isVisible = true
        }
        teams.forEachIndexed { index, team ->
            when (index) {
                0 -> {
                    team1Name.text = team.name
                    team1Name.isSelected = true
                    team1Score.text = team.score.toString()
                }
                1 -> {
                    team2Name.text = team.name
                    team2Name.isSelected = true
                    team2Score.text = team.score.toString()
                }
                2 -> {
                    team3Name.text = team.name
                    team3Name.isSelected = true
                    team3Score.text = team.score.toString()
                }
            }
        }
    }

    override fun setCurrentOtherPlayer(player: Player) {
        cardTextView.text = "${player.name} is playing"
    }

    override fun setNoCurrentPlayer() {
        setTurnStoppedState()
    }

    override fun setRound(round: String) {
        roundTextView.text = round
    }

    override fun showAllCards(cards: List<Card>) {
        val sb = java.lang.StringBuilder()
        cards.forEachIndexed { index, card ->
            sb.append("${index + 1}: ${card.name}\n")
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

    override fun navigateToEndGame() {
        findNavController().navigate(R.id.action_gameOnFragment_to_gameOverFragment,
            Bundle().apply {
                putString(GAME_ID_KEY, "dummy game id")
        })
    }

    override fun navigateToGames() {
        findNavController().navigate(R.id.action_gameOnFragment_to_GamesFragment)
    }

    override fun navigateToChooseTeam() {
        findNavController().navigate(R.id.action_gameOnFragment_to_ChooseTeamFragment)
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
        presenter.onLogout()

    override fun showCorrectCard(card: Card, videoUrl: String?) {
        showInfoToast(requireContext(), "Celebrity found: ${card.name}")
    }

    override fun showLeaveGameDialog() {
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
        builder.setMessage(getString(R.string.leave_game_dialog_title))
            .setPositiveButton(getString(R.string.ok), dialogClickListener)
            .setNegativeButton(getString(R.string.cancel), dialogClickListener)
            .show()
    }
}

val GAME_ID_KEY = "gameId"
