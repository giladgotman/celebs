package com.gggames.celebs.presentation.endturn

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.gggames.celebs.R
import com.gggames.celebs.model.Round
import com.gggames.celebs.utils.fromBundleInt
import com.gggames.celebs.utils.toBundleInt
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_end_round_dialog.*

class EndRoundDialogFragment
    : BottomSheetDialogFragment() {

    fun show(activity: AppCompatActivity) {
        show(activity.supportFragmentManager, this.javaClass.simpleName)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_end_round_dialog, container, false)

    private val KEY_ROUND_NAME = "KEY_ROUND_NAME"
    private val KEY_TEAMS = "KEY_TEAMS"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val roundName = it.getString(KEY_ROUND_NAME) ?: ""
            val score: Map<String, Int> =
                    (it.get(KEY_TEAMS) as Bundle).fromBundleInt()

            title.text = getString(R.string.end_round_title, roundName)
            team1Name.text = score.keys.toList()[0]
            team1Score.text = score[0].toString()
        }

        buttonClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }

    companion object {
        fun create(round: Round, score: Map<String, Int>): EndRoundDialogFragment {
            return EndRoundDialogFragment()
                .apply {
                isCancelable = false
                arguments =
                    Bundle().apply {
                        putString(KEY_ROUND_NAME, round.roundNumber.toString())
                        putParcelable(KEY_TEAMS, score.toBundleInt())
                    }
            }
        }

    }
}
