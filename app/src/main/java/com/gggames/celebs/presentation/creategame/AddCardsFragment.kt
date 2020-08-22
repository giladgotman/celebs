package com.gggames.celebs.presentation.creategame

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.celebs.R
import com.gggames.celebs.core.Authenticator
import com.gggames.celebs.features.cards.domain.AddCards
import com.gggames.celebs.features.cards.domain.GetMyCards
import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.features.games.data.MAX_CARDS
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.GameType
import com.gggames.celebs.model.Player
import com.gggames.celebs.presentation.MainActivity
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import com.gggames.celebs.utils.showErrorToast
import com.gggames.celebs.utils.showInfoToast
import io.reactivex.Completable
import io.reactivex.Completable.complete
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_add_cards.*
import timber.log.Timber
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddCardsFragment : Fragment() {

    @Inject
    lateinit var addCards: AddCards
    @Inject
    lateinit var getMyCards: GetMyCards
    @Inject
    lateinit var gamesRepository: GamesRepository
    @Inject
    lateinit var authenticator: Authenticator

    private var cardEditTextList = mutableListOf<EditText?>()

    private lateinit var viewComponent: ViewComponent

    private val disposables = CompositeDisposable()

    private lateinit var playerId: String

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

        (activity as MainActivity).setTitle(gamesRepository.currentGame!!.name)
        (activity as MainActivity).setShareVisible(true)

        playerId = authenticator.me!!.id

        add_cards_card6_editText.setOnEditorActionListener { v, actionId, _ ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm: InputMethodManager = v.context
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                onDoneClick()
                true
            } else false
        }
        setCardsInputFields()
        hideNonUsedCardsFields()
        buttonDone.setOnClickListener {
            onDoneClick()
        }

        navigateToGameIfCardsAreFilled()
    }

    private fun navigateToGameIfCardsAreFilled() {
        if (gamesRepository.currentGame!!.type == GameType.Gift) {
            val generatorPlayer = Player("giftGenerator", "giftGenerator")
            getMyCards(generatorPlayer).subscribe(
                { generatedCards ->
                    Timber.w("ggg celebs count: ${generatedCards.size}")
                    if (generatedCards.isNotEmpty()) navigateToChooseTeam()

                }, {}).let { disposables.add(it) }

        } else {
            getMyCards(authenticator.me!!).subscribe(
                { myCards ->
                    val cardsAlreadyFilled =
                        myCards.size >= gamesRepository.currentGame!!.celebsCount
                    if (cardsAlreadyFilled) {
                        navigateToChooseTeam()
                    }
                }, {}
            ).let { disposables.add(it) }
        }
    }

    private fun setCardsInputFields() {
        cardEditTextList.add(add_cards_card1.editText)
        cardEditTextList.add(add_cards_card2.editText)
        cardEditTextList.add(add_cards_card3.editText)
        cardEditTextList.add(add_cards_card4.editText)
        cardEditTextList.add(add_cards_card5.editText)
        cardEditTextList.add(add_cards_card6.editText)
    }

    private fun hideNonUsedCardsFields() {
        val game = gamesRepository.currentGame!!
        for (i in MAX_CARDS downTo (game.celebsCount + 1)) {
            cardEditTextList[i - 1]?.isVisible = false
        }
    }

    private fun onDoneClick() {
        val cardList = mutableListOf<Card>()

        cardEditTextList.forEach { editText ->
            editTextToCard(editText)?.let { card ->
                if (cardList.none { it.name == card.name }) {
                    cardList.add(card)
                } else {
                    editText?.error = "This name was already used"
                    return
                }
            }
        }

        tryToAddCards(cardList)
            .doOnSubscribe {
                buttonDone.isEnabled = false
            }
            .subscribe({
                navigateToChooseTeam()
            }, {
                buttonDone.isEnabled = true
                val errorMessage =
                if (it is java.lang.IllegalStateException) {
                    it.localizedMessage
                } else {
                    getString(R.string.error_generic)
                }
                showErrorToast(
                    requireContext(),
                    errorMessage,
                    Toast.LENGTH_LONG
                )
                Timber.e(it, "ggg added cards failed")
            }).let {
                disposables.add(it)
            }
    }

    private fun navigateToChooseTeam() {
        findNavController().navigate(
            R.id.action_AddCardsFragment_to_chooseTeamFragment
        )
    }

    private fun tryToAddCards(cardList: MutableList<Card>): Completable {
        return if (gamesRepository.currentGame!!.type == GameType.Gift) {
            if (gamesRepository.currentGame!!.gameInfo.totalCards > 0) {
                complete()
            } else {
                val giftCards = createAbaCards()
                addCards(giftCards)
                    .doOnComplete {
                        showInfoToast(requireContext(), "${giftCards.size} gift cards added")
                    }
            }
        } else getMyCards(authenticator.me!!)
            .flatMapCompletable { myCards ->
                if (myCards.size + cardList.size > gamesRepository.currentGame!!.celebsCount) {
                    Completable.error(IllegalStateException("you can't add ${cardList.size} more cards.\nyou already have ${myCards.size}"))
                } else {
                    addCards(cardList)
                }
            }
    }

    private fun editTextToCard(editText: EditText?): Card? {
        return if (editText?.text?.isNotEmpty() == true) {
            val cardName = editText.text.toString()
            Card(
                id = "${playerId}.$cardName",
                name = cardName,
                player = playerId
            )
        } else {
            null
        }
    }
}
