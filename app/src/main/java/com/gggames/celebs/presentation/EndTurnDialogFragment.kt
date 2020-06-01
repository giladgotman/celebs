package com.gggames.celebs.presentation

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.gggames.celebs.R
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Player
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_end_turn_dialog.*

class EndTurnDialogFragment(
    val player: Player,
    val cards: List<Card>
) : BottomSheetDialogFragment() {

    fun show(activity: AppCompatActivity) {
//        arguments = Bundle().apply { putBoolean(argumentKeyDimBehind, dimBehind)
        show(activity.supportFragmentManager, this.javaClass.simpleName)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_end_turn_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title.text = getString(R.string.end_turn_title, player.name)
        cardsAmountDescription.text = getString(R.string.end_turn_cards_description, cards.size)

        buttonDone.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
//        if (arguments?.getBoolean(argumentKeyDimBehind) == false) {
//            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
//        }
        return dialog
    }
}
