package com.gggames.celebs.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import kotlinx.android.synthetic.main.fragment_add_cards.*
import timber.log.Timber

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddCardsFragment : Fragment() {

    lateinit var addCards: AddCards
    lateinit var getMyCards: GetMyCards

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

        addCards = AddCards(
            CardsRepositoryImpl(
                FirebaseCardsDataSource(
                    gameId,
                    FirebaseFirestore.getInstance()
                )
            ),
            SchedulerProvider()
        )

        getMyCards = GetMyCards(
            CardsRepositoryImpl(
                FirebaseCardsDataSource(
                    gameId,
                    FirebaseFirestore.getInstance()
                )
            ),
            SchedulerProvider()
        )

        view.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            findNavController().navigate(R.id.action_AddCardsFragment_to_GamesFragment)
        }
        view.findViewById<Button>(R.id.buttonDone).setOnClickListener {
            val cardList = mutableListOf<Card>()
            addCardIfNotNull(editTextToCard(add_cards_card1.editText), cardList)
            addCardIfNotNull(editTextToCard(add_cards_card2.editText), cardList)
            addCardIfNotNull(editTextToCard(add_cards_card3.editText), cardList)
            addCardIfNotNull(editTextToCard(add_cards_card4.editText), cardList)
            addCardIfNotNull(editTextToCard(add_cards_card5.editText), cardList)
            addCardIfNotNull(editTextToCard(add_cards_card6.editText), cardList)

            addCards(cardList).subscribe({
                Timber.w("ggg added cards successfully")
                val args = Bundle()
                args.putStringArrayList(TEAMS_KEY, groups)
                findNavController().navigate(R.id.action_AddCardsFragment_to_chooseTeamFragment, args)
            },{
                Timber.e(it,"ggg added cards failed")
            })
        }
    }

    private fun addCardIfNotNull(card: Card?, cardList: MutableList<Card>) {
        card?.let {
            cardList.add(it)
        }
    }

    private fun editTextToCard(editText: EditText?): Card? {
        return if (editText?.text?.isNotEmpty() == true) {
            Card(editText.text.toString(), playerId)
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
