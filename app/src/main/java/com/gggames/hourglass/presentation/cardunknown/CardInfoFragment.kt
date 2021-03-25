package com.gggames.hourglass.presentation.cardunknown

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.gggames.hourglass.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_card_info.*

class CardInfoFragment : BottomSheetDialogFragment() {

    fun show(activity: AppCompatActivity) {
        show(activity.supportFragmentManager, this.javaClass.simpleName)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_card_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closeButton.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun newInstance(cardName: String) =
            CardInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_CARD_NAME, cardName)
                }
            }

        const val KEY_CARD_NAME = "KEY_CARD_NAME"
    }

}