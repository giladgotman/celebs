package com.gggames.hourglass.presentation.endturn

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.gggames.hourglass.R
import com.gggames.hourglass.model.Player
import com.gggames.hourglass.model.Round
import com.gggames.hourglass.model.Team
import com.gggames.hourglass.presentation.gameon.GameScreenContract
import com.gggames.hourglass.presentation.onboarding.ViewPagerFragment
import com.gggames.hourglass.presentation.onboarding.ViewPagerFragmentAdapter
import com.gggames.hourglass.utils.rx.EventEmitter
import com.gggames.hourglass.utils.rx.ViewEventEmitter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_round_change.*

class ChangeRoundDialogFragment :
    BottomSheetDialogFragment(), EventEmitter<GameScreenContract.UiEvent> by ViewEventEmitter() {

    private lateinit var onDismissBlock: () -> Unit

    fun show(activity: AppCompatActivity) {
        show(activity.supportFragmentManager, this.javaClass.simpleName)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_round_change, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val currentRound: Round = it.getParcelable(KEY_CURR_ROUND)!!
            val isRoundOver = it.getBoolean(KEY_ROUND_OVER)
            val nextPlayer: Player? = it.getParcelable(KEY_NEXT_PLAYER)
            val teams = it.getParcelableArray(KEY_TEAMS) as Array<Team>? ?: emptyArray()
            initializeCarouselViewPager(currentRound, isRoundOver, teams.toList(), nextPlayer)

            buttonNextRound.setOnClickListener {
                if (view_pager_carousel.currentItem == 0) {
                    view_pager_carousel.setCurrentItem(1, true)
                    buttonNextRound.text = getString(R.string.change_round_next_round_cta)
                } else {
                    dismiss()
                }
            }
            if (!isRoundOver) {
                buttonNextRound.text = getString(R.string.change_round_next_round_cta)
                buttonNextRound.setOnClickListener {
                    dismiss()
                }
            }
        }
    }

    fun setOnDismiss(block: () -> Unit) {
        onDismissBlock = block
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        GameScreenContract.UiEvent.RoundOverDialogDismissed.emit()
        onDismissBlock()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        setupBottomSheet(dialog)
        return dialog
    }

    private fun setupBottomSheet(dialog: Dialog) {
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet!!)
            behavior.isHideable = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun initializeCarouselViewPager(
        currentRound: Round,
        roundOver: Boolean,
        teamsWithScore: List<Team>,
        nextPlayer: Player?
    ) {
        val carouselItems: List<ViewPagerFragment> =
            if (roundOver) {
                val nextRoundId = currentRound.roundNumber + 1
                val roundName = roundIdToRoundName(nextRoundId)
                listOf(
                    ViewPagerFragment(EndRoundDialogFragment(), Bundle().apply {
                        putParcelable(KEY_CURR_ROUND, currentRound)
                        putParcelableArray(KEY_TEAMS, teamsWithScore.toTypedArray())
                    }),
                    ViewPagerFragment(
                        NextRoundDialogFragment(),
                        NextRoundDialogFragment.createArgs(nextRoundId, roundName, currentRound.turn)
                    )
                )
            } else {
                val nextRoundId = currentRound.roundNumber
                val roundName = roundIdToRoundName(nextRoundId)
                listOf(
                    ViewPagerFragment(
                        NextRoundDialogFragment(),
                        NextRoundDialogFragment.createArgs(nextRoundId, roundName, currentRound.turn.copy(player = nextPlayer))
                    )
                )
            }
        view_pager_carousel.isUserInputEnabled = false
        view_pager_carousel.adapter =
            ViewPagerFragmentAdapter(requireActivity().supportFragmentManager, lifecycle, carouselItems)



        if (carouselItems.size > 1) {
            view_pager_carousel.offscreenPageLimit = carouselItems.size - 1
        }
    }

    private fun roundIdToRoundName(nextRoundId: Int): String {
        return when (nextRoundId) {
            1 -> "Describe"
            2 -> "One Word"
            3 -> "Charades"
            else -> throw IllegalArgumentException("Unknown supported number: $nextRoundId")
        }
    }

    companion object {
        fun newInstance(
            prevRound: Round,
            roundOver: Boolean,
            teamsWithScore: List<Team>,
            nextPlayer: Player? = null
        ): ChangeRoundDialogFragment {
            return ChangeRoundDialogFragment()
                .apply {
                    isCancelable = true
                    arguments =
                        Bundle().apply {
                            putParcelable(KEY_CURR_ROUND, prevRound)
                            nextPlayer?.let {
                                putParcelable(KEY_NEXT_PLAYER, it)
                            }
                            putBoolean(KEY_ROUND_OVER, roundOver)
                            putParcelableArray(KEY_TEAMS, teamsWithScore.toTypedArray())
                        }
                }
        }
    }
}

val KEY_CURR_ROUND = "KEY_CURR_ROUND"
val KEY_NEXT_PLAYER = "KEY_NEXT_PLAYER"
val KEY_TEAMS = "KEY_TEAMS"
val KEY_ROUND_OVER = "KEY_ROUND_OVER"
