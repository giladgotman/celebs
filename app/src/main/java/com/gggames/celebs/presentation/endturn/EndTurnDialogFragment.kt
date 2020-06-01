package com.gggames.celebs.presentation.endturn

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gggames.celebs.R
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Player
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_end_turn_dialog.*
import timber.log.Timber


class EndTurnDialogFragment
    : BottomSheetDialogFragment() {

    private lateinit var cardsFoundAdapter: CardsFoundAdapter


    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        override fun onStateChanged(
            bottomSheet: View,
            newState: Int
        ) {
            Timber.w("onStateChanged : $newState")
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(
            bottomSheet: View,
            slideOffset: Float
        ) {
            Timber.w("onSlide : $slideOffset")
//            behavior.state = BottomSheetBehavior.STATE_EXPANDED;
        }
    }
    fun show(activity: AppCompatActivity) {
        show(activity.supportFragmentManager, this.javaClass.simpleName)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_end_turn_dialog, container, false)

    private val KEY_PLAYER_NAME = "KEY_PLAYER_NAME"
    private val KEY_CARD_NAMES = "KEY_CARD_NAMES"

    lateinit var behavior: BottomSheetBehavior<*>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardsFoundAdapter = CardsFoundAdapter {
            Timber.w("on card click : ${it.name}")
        }

//        behavior =
//            BottomSheetBehavior.from(bottomSheetLayout)
//        behavior.isFitToContents = true
//        behavior.state = BottomSheetBehavior.STATE_EXPANDED;
//
//        behavior.addBottomSheetCallback(mBottomSheetBehaviorCallback)


        cardsRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this.context)
        cardsRecyclerView.layoutManager = layoutManager
        cardsRecyclerView.itemAnimator = DefaultItemAnimator()
        cardsRecyclerView.adapter = cardsFoundAdapter

        arguments?.let {
            val name = it.getString(KEY_PLAYER_NAME) ?: ""
            val cardsNames: ArrayList<String>? =
                it.getStringArrayList(KEY_CARD_NAMES) as ArrayList<String>

//            cardsFoundAdapter.setData(cardsNames!!.map { Card(name = it, player = name) })
            cardsFoundAdapter.setData(
                listOf(
                    Card(name = "1dfssd", player = ""),
                    Card(name = "1dfsdfssd", player = ""),
                    Card(name = "1dddfsfssd", player = ""),
                    Card(name = "1dfddssd", player = ""),
                    Card(name = "1dfdsfsdsd", player = ""),
                    Card(name = "1dfddssd", player = ""),
                    Card(name = "1df  ssd", player = ""),
                    Card(name = "1dfs   sd", player = ""),
                    Card(name = "1dfsdddddsd", player = "")
                )
            )
            title.text = getString(R.string.end_turn_title, name)
            cardsAmountDescription.text =
                getString(R.string.end_turn_cards_description, cardsNames?.size ?: 0)
        }

        buttonDone.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }

    companion object {
        fun create(player: Player, cards: List<Card>): EndTurnDialogFragment {
            return EndTurnDialogFragment()
                .apply {
                isCancelable = false
                arguments =
                    Bundle().apply {
                        putString(KEY_PLAYER_NAME, player.name)
                        putStringArrayList(KEY_CARD_NAMES, ArrayList(cards.map { it.name }))
                    }
            }
        }

    }
}
