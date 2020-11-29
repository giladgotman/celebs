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
import com.gggames.hourglass.model.Round
import com.gggames.hourglass.model.Team
import com.gggames.hourglass.model.roundIdToName
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
            val teams = it.getParcelableArray(KEY_TEAMS) as Array<Team>? ?: emptyArray()
            initializeCarouselViewPager(currentRound, teams.toList())

            button_ready.setOnClickListener {
                if (view_pager_carousel.currentItem == 0) {
                    view_pager_carousel.setCurrentItem(1, true)
                    button_ready.text = getString(R.string.change_round_next_round_cta)
                } else {
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
        teamsWithScore: List<Team>
    ) {
        val nextRoundId = currentRound.roundNumber + 1
        val roundName = roundIdToName(nextRoundId)
        val carouselItems: List<ViewPagerFragment> = listOf(
            ViewPagerFragment(EndRoundDialogFragment(), Bundle().apply {
                putParcelable(KEY_CURR_ROUND, currentRound)
                putParcelableArray(KEY_TEAMS, teamsWithScore.toTypedArray())
            }),
            ViewPagerFragment(
                NextRoundDialogFragment(),
                NextRoundDialogFragment.createArgs(nextRoundId, roundName, currentRound.turn)
            )
        )
//        view_pager_carousel.setPageTransformer { page, position ->
//            page.alpha = 0f
//            page.visibility = View.VISIBLE
//
//            // Start Animation for a short period of time
//            page.animate()
//                .alpha(1f).duration = 300
//        }
        view_pager_carousel.isUserInputEnabled = false
        view_pager_carousel.adapter =
            ViewPagerFragmentAdapter(requireActivity().supportFragmentManager, lifecycle, carouselItems)

        if (carouselItems.size > 1) {
            view_pager_carousel.offscreenPageLimit = carouselItems.size - 1
        }
    }

    companion object {
        fun newInstance(
            prevRound: Round,
            roundOver: Boolean,
            teamsWithScore: List<Team>
        ): ChangeRoundDialogFragment {
            return ChangeRoundDialogFragment()
                .apply {
                    isCancelable = true
                    arguments =
                        Bundle().apply {
                            putParcelable(KEY_CURR_ROUND, prevRound)
                            putBoolean(KEY_ROUND_OVER, roundOver)
                            putParcelableArray(KEY_TEAMS, teamsWithScore.toTypedArray())
                        }
                }
        }
    }
}

val KEY_CURR_ROUND = "KEY_CURR_ROUND"
val KEY_TEAMS = "KEY_TEAMS"
val KEY_ROUND_OVER = "KEY_ROUND_OVER"
