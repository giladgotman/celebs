package com.gggames.hourglass.presentation.endturn

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.gggames.hourglass.R
import com.gggames.hourglass.model.Team
import com.gggames.hourglass.presentation.gameon.GameScreenContract
import com.gggames.hourglass.utils.rx.EventEmitter
import com.gggames.hourglass.utils.rx.ViewEventEmitter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_end_round_dialog.*

class ChangeRoundDialogFragment :
    BottomSheetDialogFragment(), EventEmitter<GameScreenContract.UiEvent> by ViewEventEmitter() {

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
        buttonClose.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        GameScreenContract.UiEvent.RoundOverDialogDismissed.emit()
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }

    companion object {
        fun create(endedRoundName: String, teams: List<Team>): ChangeRoundDialogFragment {
            return ChangeRoundDialogFragment()
                .apply {
                isCancelable = true
                arguments =
                    Bundle().apply {
                    }
            }
        }
    }
}
