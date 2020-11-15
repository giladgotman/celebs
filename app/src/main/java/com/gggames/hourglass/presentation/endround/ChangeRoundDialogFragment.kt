package com.gggames.hourglass.presentation.endturn

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.gggames.hourglass.R
import com.gggames.hourglass.model.Round
import com.gggames.hourglass.model.Team
import com.gggames.hourglass.presentation.gameon.GameScreenContract
import com.gggames.hourglass.presentation.onboarding.ViewPagerFragment
import com.gggames.hourglass.presentation.onboarding.ViewPagerFragmentAdapter
import com.gggames.hourglass.utils.rx.EventEmitter
import com.gggames.hourglass.utils.rx.ViewEventEmitter
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
//        buttonClose.setOnClickListener {
//            dismissAllowingStateLoss()
//        }

        arguments?.let {
            val prevRoundName: String? = it.getString(KEY_PREV_ROUND_NAME)
            val nextRound: Round = it.getParcelable(KEY_NEXT_ROUND)!!
            val teams = it.getParcelableArray(KEY_TEAMS) as Array<Team>? ?: emptyArray()
            initializeCarouselViewPager(prevRoundName, nextRound, teams.toList())
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
        return dialog
    }

    private fun initializeCarouselViewPager(prevRoundName: String?, nextRound: Round, teamsWithScore: List<Team>) {
        val carouselItems: List<ViewPagerFragment> =
            if (prevRoundName == null) {
                listOf(
                    ViewPagerFragment(EndRoundDialogFragment(), Bundle().apply {
                        putString(KEY_PREV_ROUND_NAME, prevRoundName)
                        putParcelableArray(KEY_TEAMS, teamsWithScore.toTypedArray())
                    })
                )
            } else {
                listOf(
                    ViewPagerFragment(EndRoundDialogFragment(), Bundle().apply {
                        putString(KEY_PREV_ROUND_NAME, prevRoundName)
                    }),
                    ViewPagerFragment(EndRoundDialogFragment(), Bundle().apply {
                        putString(KEY_PREV_ROUND_NAME, prevRoundName)
                    })
                )
            }
        view_pager_carousel.adapter =
            ViewPagerFragmentAdapter(requireActivity().supportFragmentManager, lifecycle, carouselItems)



        if (carouselItems.size > 1) {
            view_pager_carousel.offscreenPageLimit = carouselItems.size - 1
//            carousel_indicator.setViewPager(view_pager_carousel)
        }
    }

    companion object {
        fun create(prevRoundName: String?, nextRound: Round, teamsWithScore: List<Team>): ChangeRoundDialogFragment {
            return ChangeRoundDialogFragment()
                .apply {
                    isCancelable = true
                    arguments =
                        Bundle().apply {
                            prevRoundName?.let {
                                putString(KEY_PREV_ROUND_NAME, prevRoundName)
                            }
                            putParcelable(KEY_NEXT_ROUND, nextRound)
                            putParcelableArray(KEY_TEAMS, teamsWithScore.toTypedArray())
                        }
                }
        }
    }
}

val KEY_PREV_ROUND_NAME = "KEY_PREV_ROUND"
val KEY_NEXT_ROUND = "KEY_PREV_ROUND"
val KEY_TEAMS = "KEY_TEAMS"
