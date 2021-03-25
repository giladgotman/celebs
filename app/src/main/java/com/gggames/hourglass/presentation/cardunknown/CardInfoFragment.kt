package com.gggames.hourglass.presentation.cardunknown

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.gggames.hourglass.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
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

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closeButton.setOnClickListener {
            dismiss()
        }

        arguments?.let { bundle ->
            val cardName = bundle.getString(KEY_CARD_NAME)!!
            title.text = cardName
            val url = "https://www.google.com/search?q=$cardName"
            webview.settings.javaScriptEnabled = true

            webview.loadUrl(url)
        }
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
            behavior.isHideable = false
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
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