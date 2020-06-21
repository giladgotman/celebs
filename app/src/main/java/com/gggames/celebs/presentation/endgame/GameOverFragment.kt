package com.gggames.celebs.presentation.endgame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gggames.celebs.R
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import kotlinx.android.synthetic.main.fragment_game_over.*
import javax.inject.Inject

class GameOverFragment : Fragment() {

    @Inject
    lateinit var presenter: GameOverPresenter
    private lateinit var viewComponent: ViewComponent


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_over, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewComponent = createViewComponent(this)
        viewComponent.inject(this)

        subtitle.text = getString(R.string.game_over_subtitle, "team 1 dummy")
        presenter.bind()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.unBind()
    }
}