package com.gggames.hourglass.presentation.creategame

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.hourglass.R
import com.gggames.hourglass.features.games.data.MAX_CARDS
import com.gggames.hourglass.model.Card
import com.gggames.hourglass.presentation.MainActivity
import com.gggames.hourglass.presentation.di.ViewComponent
import com.gggames.hourglass.presentation.di.createViewComponent
import com.gggames.hourglass.utils.createToolTip
import com.gggames.hourglass.utils.showErrorToast
import com.skydoves.balloon.*
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_cards.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AddCardsFragment : Fragment(), AddCardsPresenter.View {

    @Inject
    lateinit var presenter: AddCardsPresenter

    private var cardEditTextList = mutableListOf<EditText?>()

    private lateinit var viewComponent: ViewComponent

    private val disposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_cards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewComponent = createViewComponent(this)
        viewComponent.inject(this)

        (activity as MainActivity).setTitle(getString(R.string.add_cards_title))
        (activity as MainActivity).setShareVisible(true)

        add_cards_card6_editText.setOnEditorActionListener { v, actionId, _ ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm: InputMethodManager = v.context
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                onDoneClick()
                true
            } else false
        }
        buttonDone.setOnClickListener {
            onDoneClick()
        }

        val showPopup = arguments?.getBoolean(KEY_SHOW_SHARE_POPUP, false) ?: false
        presenter.bind(this, showPopup)
    }


    override fun showCards(cards: List<Card>, cardsLimit: Int) {
        setCardsInputFields()
        hideNonUsedCardsFields(cardsLimit)

        cards.forEachIndexed { index, card ->
            cardEditTextList[index]?.setText(card.name)
            cardEditTextList[index]?.isEnabled = false
        }

    }

    override fun enableDone(enable: Boolean) {
        buttonDone.isEnabled = enable
    }


    override fun showSharePopup(gameName: String) {
        ShareGameFragment.newInstance(gameName).show((requireActivity() as AppCompatActivity),
            onClick = {
                Observable.timer(2, TimeUnit.SECONDS)
                    .subscribe {
                        showTooltip(setupToolTip(), requireActivity().button_share)
                    }.let { disposables.add(it) }
                (requireActivity() as MainActivity).shareGame()
            },
            onDismiss = {
                showTooltip(setupToolTip(), requireActivity().button_share)
            })
    }

    private fun setCardsInputFields() {
        cardEditTextList.add(add_cards_card1.editText)
        cardEditTextList.add(add_cards_card2.editText)
        cardEditTextList.add(add_cards_card3.editText)
        cardEditTextList.add(add_cards_card4.editText)
        cardEditTextList.add(add_cards_card5.editText)
        cardEditTextList.add(add_cards_card6.editText)
    }

    private fun hideNonUsedCardsFields(cardsLimit: Int) {
        for (i in MAX_CARDS downTo (cardsLimit + 1)) {
            cardEditTextList[i - 1]?.isVisible = false
        }
    }

    private fun onDoneClick() {
        val cardList = mutableListOf<String>()
        cardEditTextList.forEach { editText ->
            val cardName = editTextToString(editText)
            cardName?.let {
                if (!cardList.contains(cardName)) {
                    cardList.add(cardName)
                } else {
                    editText?.error = "This name was already used"
                    return
                }
            }
        }
        presenter.onDoneClicked(cardList)
    }

    private fun editTextToString(editText: EditText?): String? {
        return if (editText?.text?.isNotEmpty() == true) {
            return editText.text.toString()
        } else {
            null
        }
    }

    override fun showError(errorText: String) {
        showErrorToast(
            requireContext(),
            errorText,
            Toast.LENGTH_LONG
        )
    }

    override fun navigateToChooseTeam() {
        findNavController().navigate(
            R.id.action_AddCardsFragment_to_chooseTeamFragment
        )
    }

    private fun navigateToGameOn() {
        findNavController().navigate(
            R.id.action_AddCardsFragment_to_gameOnFragment
        )
    }


    private fun setupToolTip() =
        createToolTip(
            requireContext(),
            ArrowOrientation.BOTTOM,
            "Anyone can share the game at anytime",
            lifecycleOwner = viewLifecycleOwner,
            animation = BalloonAnimation.FADE
        )

    private fun showTooltip(balloon: Balloon, view: View) {
        balloon.setOnBalloonClickListener {
            balloon.dismiss()
        }

        balloon.setOnBalloonDismissListener {
            // doSomething;
        }

        balloon.setOnBalloonOutsideTouchListener { _, _ ->
            balloon.dismiss()
        }

        balloon.showAlignBottom(view)
    }

    companion object {
        const val KEY_SHOW_SHARE_POPUP = "KEY_SHOW_SHARE_POPUP"
    }
}
