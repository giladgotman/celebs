package com.gggames.celebs.presentation.gameon

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.fragment_game_on.view.*
import javax.inject.Inject

class GameOnUiBinder @Inject constructor() {

    private lateinit var fragment: GameOnFragmentMVI

    private var view: View? = null

    private var context: Context? = null


    fun render(state: GameScreenContract.State) {
        view?.cardTextView?.text = state.currentCard?.name ?: ""
        view?.cardsAmount?.text = state.cardsInDeck.toString()
    }

    fun setFragment(fragment: GameOnFragmentMVI) {
        this.fragment = fragment
        view = fragment.activity?.window?.decorView
        context = view?.context
    }

}