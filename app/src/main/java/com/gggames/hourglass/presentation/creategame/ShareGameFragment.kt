package com.gggames.hourglass.presentation.creategame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.gggames.hourglass.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_share_game.*
import kotlinx.android.synthetic.main.fragment_welcome_first_round.button_ready

class ShareGameFragment : BottomSheetDialogFragment() {

     private var onClick : (()-> Unit)? = null

    fun show(activity: AppCompatActivity, onClick: ()-> Unit) {
        this.onClick = onClick
        show(activity.supportFragmentManager, this.javaClass.simpleName)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_share_game, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val gameName = it.getString(KEY_GAME_NAME, null)
            gameName?.let {
                description.text = getString(R.string.share_frag_description, gameName)
            }
        }
        button_ready.setOnClickListener {
            dismiss()
            onClick?.let { it() }
            onClick = null
        }
    }

    companion object {
        fun newInstance(gameName: String) =
            ShareGameFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_GAME_NAME, gameName)
                }
            }
    }

    val KEY_GAME_NAME = "KEY_GAME_NAME"
}


