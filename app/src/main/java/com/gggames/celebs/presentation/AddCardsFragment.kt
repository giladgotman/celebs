package com.gggames.celebs.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.celebs.R
import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.data.cards.CardsRepositoryImpl
import com.gggames.celebs.data.model.Card
import com.gggames.celebs.data.source.remote.FirebaseCardsDataSource
import com.gggames.celebs.domain.cards.AddCards
import com.gggames.celebs.domain.cards.GetMyCards
import com.google.firebase.firestore.FirebaseFirestore
import com.idagio.app.core.utils.rx.scheduler.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_add_cards.*
import timber.log.Timber

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddCardsFragment : Fragment() {

    lateinit var addCards: AddCards
    lateinit var getMyCards: GetMyCards

    lateinit var cardsRepositoryImpl: CardsRepositoryImpl
    lateinit var firebaseCardsDataSource: FirebaseCardsDataSource
    private val schedulerProvider = SchedulerProvider()

    private val disposables = CompositeDisposable()

    lateinit var gameId: String
    lateinit var playerId: String
    lateinit var groups: ArrayList<String>

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_cards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            gameId = it.getString(GAME_ID_KEY)!!
            groups = it.getStringArrayList(TEAMS_KEY)!!
        }

        playerId = GameFlow.me!!.id

        firebaseCardsDataSource = FirebaseCardsDataSource(gameId, FirebaseFirestore.getInstance())
        cardsRepositoryImpl = CardsRepositoryImpl(firebaseCardsDataSource)

        addCards = AddCards(cardsRepositoryImpl, schedulerProvider)
        getMyCards = GetMyCards(cardsRepositoryImpl, schedulerProvider)

        add_cards_card6_editText.setOnEditorActionListener { v, actionId, event ->
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
    }

    private fun onDoneClick() {
        buttonDone.isEnabled = false
        val cardList = mutableListOf<Card>()
        addCardIfNotNull(editTextToCard(add_cards_card1.editText), cardList)
        addCardIfNotNull(editTextToCard(add_cards_card2.editText), cardList)
        addCardIfNotNull(editTextToCard(add_cards_card3.editText), cardList)
        addCardIfNotNull(editTextToCard(add_cards_card4.editText), cardList)
        addCardIfNotNull(editTextToCard(add_cards_card5.editText), cardList)
        addCardIfNotNull(editTextToCard(add_cards_card6.editText), cardList)



        tryToAddCards(cardList)
            .subscribe({
                Timber.w("ggg added cards successfully")
                val args = Bundle()
                args.putStringArrayList(TEAMS_KEY, groups)
                findNavController().navigate(
                    R.id.action_AddCardsFragment_to_chooseTeamFragment,
                    args
                )
            }, {
                buttonDone.isEnabled = true
                val errorMessage =
                if (it is java.lang.IllegalStateException) {
                    it.localizedMessage
                } else {
                    getString(R.string.error_generic)
                }
                Toast.makeText(
                    requireContext(),
                    errorMessage,
                    Toast.LENGTH_LONG
                )
                    .show()
                Timber.e(it, "ggg added cards failed")
            }).let {
                disposables.add(it)
            }
    }

    private fun tryToAddCards(cardList: MutableList<Card>): Completable {
        return getMyCards(GameFlow.me!!)
            .flatMapCompletable { myCards ->
                if (myCards.size + cardList.size > GameFlow.currentGame!!.celebsCount) {
                    Completable.error(IllegalStateException("you can't add ${cardList.size} more cards. you already have ${myCards.size}."))
                } else {
                    addCards(cardList)
                        .compose(schedulerProvider.applyCompletableDefault())
                }
            }
    }

    private fun addCardIfNotNull(card: Card?, cardList: MutableList<Card>) {
        card?.let {
            cardList.add(it)
        }
    }

    private fun editTextToCard(editText: EditText?): Card? {
        return if (editText?.text?.isNotEmpty() == true) {
            Card(name = editText.text.toString(), player = playerId)
        } else {
            null
        }
    }
    companion object {
        fun createArgs(gameId: String, teams: ArrayList<String>, playerId: String): Bundle {
            return Bundle().apply {
                putString(GAME_ID_KEY, gameId)
                putStringArrayList(TEAMS_KEY, teams)
                putString(PLAYER_ID_KEY, playerId)
            }
        }
    }
}

const val GAME_ID_KEY = "GAME_ID_KEY"
const val PLAYER_ID_KEY = "PLAYER_ID_KEY"
const val PLAYER_NAME_KEY = "PLAYER_NAME_KEY"
