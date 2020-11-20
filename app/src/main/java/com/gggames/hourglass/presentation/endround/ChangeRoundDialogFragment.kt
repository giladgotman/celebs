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
        buttonNextRound.setOnClickListener {
            if (view_pager_carousel.currentItem == 0) {
                view_pager_carousel.setCurrentItem(1, true)
                buttonNextRound.text = "START ROUND"
            } else {
                dismiss()
            }
        }

        arguments?.let {
            val prevRound: Round? = it.getParcelable(KEY_PREV_ROUND)
            val teams = it.getParcelableArray(KEY_TEAMS) as Array<Team>? ?: emptyArray()
            initializeCarouselViewPager(prevRound, teams.toList())
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

    private fun initializeCarouselViewPager(prevRound: Round?, teamsWithScore: List<Team>) {
        val carouselItems: List<ViewPagerFragment> =
            if (prevRound == null) {
                listOf(
                    ViewPagerFragment(EndRoundDialogFragment(), Bundle().apply {
                        putParcelable(KEY_PREV_ROUND, prevRound)
                        putParcelableArray(KEY_TEAMS, teamsWithScore.toTypedArray())
                    })
                )
            } else {
                listOf(
                    ViewPagerFragment(EndRoundDialogFragment(), Bundle().apply {
                        putParcelable(KEY_PREV_ROUND, prevRound)
                        putParcelableArray(KEY_TEAMS, teamsWithScore.toTypedArray())
                    }),
                    ViewPagerFragment(NextRoundDialogFragment(), Bundle().apply {
                        putParcelable(KEY_PREV_ROUND, prevRound)
                    })
                )
            }
        view_pager_carousel.isUserInputEnabled = false
        view_pager_carousel.adapter =
            ViewPagerFragmentAdapter(requireActivity().supportFragmentManager, lifecycle, carouselItems)



        if (carouselItems.size > 1) {
            view_pager_carousel.offscreenPageLimit = carouselItems.size - 1
//            carousel_indicator.setViewPager(view_pager_carousel)
        }
    }

    companion object {
        fun create(prevRound: Round?, teamsWithScore: List<Team>): ChangeRoundDialogFragment {
            return ChangeRoundDialogFragment()
                .apply {
                    isCancelable = true
                    arguments =
                        Bundle().apply {
                            prevRound?.let {
                                putParcelable(KEY_PREV_ROUND, prevRound)
                            }
                            putParcelableArray(KEY_TEAMS, teamsWithScore.toTypedArray())
                        }
                }
        }
    }
}

val KEY_PREV_ROUND = "KEY_PREV_ROUND"
val KEY_TEAMS = "KEY_TEAMS"
