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
import com.gggames.celebs.presentation.MainActivity
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import com.gggames.celebs.utils.showErrorToast
import io.reactivex.Completable
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
        getMyCards(authenticator.me!!).subscribe(
            {
                if (it.size >= gamesRepository.currentGame!!.celebsCount) {
                    navigateToChooseTeam()
                }
            }, {}
        ).let { disposables.add(it) }
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

    val giftGame = true
    private fun tryToAddCards(cardList: MutableList<Card>): Completable {
        return if (giftGame) {
                val giftList = createAbaCards()
                addCards(giftList)
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
            Card(
                name = editText.text.toString(),
                player = playerId
            )
        } else {
            null
        }
    }

    fun createAbaCards(): List<Card> {
        return listOf(
            Card(
                "2", "שולה", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1oGsGlU5EyWIG7rSL2sBTxC_qnW4fvR7V",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1sFd1-HqZubsjv2wNtAkIRSY3uEufXug7",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=10-yyxX0JcALNyuQ4nj8EGUgjG2sVCqht",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=1DPs7j14I7cYXLCUEnjZW7ep8fIksUj28"
            )
            ,
            Card(
                "3", "רוני", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1j6epbAZJ8f-uXUveE-7dxYfJqz9hqiqA",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1881DeQ2Hpq37a99tGzzjCnUcGr_AVif7",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1Sl0kGMCV0JEPhxZDT29ndge4fdAoElU-",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=1cn3o_DOl8VeKtRiGHVQpaXv5rK9T-6Gd"
            ),
            Card(
                "4", "יוסי ונחמה", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1AlmdQNVg8fZ7qy-qHQ68hfS5ih1aTFz0",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=17LjJIiMRy5PceGDc8Qc5hnYMoe5CSXoc",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=11Ai4ocMeLH9otGxpQ9t_uS0jGjSSPN6e",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=1-ir5S6SEbIK3D4_wc20MlrNQf-Z07_Sd"
            ),
            Card(
                "5", "דני ארנהלט", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=18ciki2ZwoO73qHv46w1ohoemq8ibB9rO",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1XuEGppNMMpb6J2oYVJ_sAjT1j4PWg4iH",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1j3_nbcZfcO4jPN2VD1mUpsOCapgqJlWT",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=16qWlgUd8ptiZOjGmPAbLUHbjcZsKBh2Z"
            ),
            Card(
                "6", "דגנית", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1XIAyE6iMfOYwTYiphCUcAnhQBOaUaLEL",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1k2ZqunpmeNOIcZTnf0JF-ELxXZ-NUbUr",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1nbZYycfFkTLiO6YcVnQAKTHDCU6-BEex",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=1eV597HTFi0vSrI27et766c-aI_nqqll6"
            ),
            Card(
                "7", "יאקיס", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1Ssy0A3xmUQtBzoqxrpoFGl-GFppGUls3",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1x8mDkcgIPkVped6Ti1Tb8SdEtTNUYDZ1",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1bt-yMoI-rJZ2g1lqg3X284pDi8vEfwTW",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=1CfGfuLZ10Figvy9zFb2Bi12hWK0bK9R9"
            ),
            Card(
                "8", "ישראל ואילה רשף", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1TPeBuoM4rrMHoK2OHlijNm3u1ED1Lknl",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1SdKSfgwQLpvSBiBV7lhtwtIVUq6zdL4w",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1zGlpWYFk9QGfBzQwAP7MobUI0vvHw0r1",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=16vUNsJtq3WRpHrkaojp6_N3bRdyJhkUd"
            ),
            Card(
                "9", "ויולה", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=194rl8msLR47b8No3-uuI-AmLre2wgoC9",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=147xu8GaVe25o3LhJ6xNcElqeEEHD6_vW",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1CGIg6YgKin7m-QmHvyQ03omj6yEvWFRG",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=1k-6jLFqi7YO_QgeCfA_ubU22_vLY-2AO"
            ),
            Card(
                "10", "כרמית", "p",
                videoUrl1 = "text",
                videoUrl2 = "",
                videoUrl3 = "",
                videoUrlFull = ""
            ),
            Card(
                "11", "מיקי", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1y2L1qDuA9bzs9M8MC6UXmLERuhcP4AMn",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1y2L1qDuA9bzs9M8MC6UXmLERuhcP4AMn",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=16xdGHrq3JQaxYnVAm8DaXVVcqqSgOtHF",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=16xdGHrq3JQaxYnVAm8DaXVVcqqSgOtHF"
            ),
            Card(
                "12", "גלעד", "p",
                videoUrl1 = "",
                videoUrl2 = "",
                videoUrl3 = "",
                videoUrlFull = ""
            ),
            Card(
                "13", "בילי", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1drlnGyJFU5uCPs0x6uGG4Hwubs670ePJ",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1oqF6BD_WkbckdCrMVQ5fMRk4azpdjXjC",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1fcfEkHCq44yRexexK0jxIpC87qM7cv83",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=16lXMvAdxFFBXIRIzCqjsZ4ebGsQI2GGI"
            ),
            Card(
                "14", "דן מלר", "p",
                videoUrl1 = "text",
                videoUrl2 = "",
                videoUrl3 = "",
                videoUrlFull = ""
            ),
            Card(
                "15", "מוריס בן זקן", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=14r8TLnj9k8NvQzmI_dlxf03RkA3sVhUH",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1RQdsSEws8Hgc16KjYcA9UiWZs55Y9gZQ",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1DseLXlyCktMesstAdnJo5fD-LVVXAlTQ",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=14VwIZhNM0LndRuYKciyPRgrSwKSQmGdK"
            ),
            Card(
                "16", "ליה", "p",
                videoUrl1 = "",
                videoUrl2 = "",
                videoUrl3 = "",
                videoUrlFull = ""
            ),
            Card(
                "17", "רן", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1H0zyRn7TsX2vXiBb4SaIjHu_qsF8ryi-",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1R9EP632Ql7FxQO0h8_kilAg5ASr-6KuI",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1hY0QSecqXlv6zPrL_ux253BUcpLMDegQ",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=16onrjT5SaZML6nBRbYvDvQqefQi5Nc6D"
            ),
            Card(
                "18", "ורד צלחת עם מכסה לחתול", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1QhgoOWYkVvZscFtBAjr1gMzVjyDUs2VA",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1zFiXOGyYDNSKfojbCYxARma150OmXbbu",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1qt08YK5-ugMc0XTevmR5OeeuzpM4txsz",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=11HeE_61EUUwp3IGSYK1xvk7H5qLg-aW0"
            ),
            Card(
                "19", "אילן גבאי", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1K7NHNVSroyfexy1ueotq3jiIn1HFmkyD",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=16Lumnf0gp32de-A-8VbTid3O-v1UTYCd",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1Y01_NkBVaP12r7MlGT0QtUNv6RfPPvdL",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=16vKFmMDCYZUUyex227KpIkPhk5IBhKle"
            ),
            Card(
                "20", "ערן שוורץ", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1uXjTBqX7jYDUZ9GAw_JkTSJIub58gbdT",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1sKR-KtH45xwc5DmqyHkZ4seN7XwUdU1p",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1EUgWgwgqqG0zhJM2MNiU7AGhNLSIekqc",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=10HVoxZdDSS1nSK2Gf77nonUQtNKLPnOq"
            ),
            Card(
                "21", "נופר", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1cgLKkArQW1aSQiwRG2OEL15W2Ci7FYm5",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1ZoIAd4zZeJMi715Pz-jRJORPp9K9d3Zw",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1UBIXtChyAH3sMsNezDqaEkKfR_eompv1",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=17DAf6fsAmLSfLuqvsrfztKe-z76l6JWT"
            ),
            Card(
                "22", "רון", "p",
                videoUrl1 = "text",
                videoUrl2 = "",
                videoUrl3 = "",
                videoUrlFull = ""
            ),
            Card(
                "23", "לוסי", "p",
                videoUrl1 = "",
                videoUrl2 = "",
                videoUrl3 = "",
                videoUrlFull = ""
            ),
            Card(
                "24", "אטי", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1uOZduEr9tAd-wg7E7874ipulYBcnJCUs",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=104-Ya4kU3mFWP5FBA2kaxbv7ljqa6fsQ",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1LUks5pYRH0NP0unoDnUFYCQP4KfpnXMh",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=14Y-Cij8k81UUlOOOEtinsMzeWmyP99cT"
            ),
            Card(
                "25", "נדב ולימור תירוש", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1DL6_b488cThfFVdU5NpcsrBf79sBm6ow",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1TGV8qm3jNCe06N4LLDO2JKZKCBo6VOhA",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1qNEbhC5FF7vXPuD6LVqHE5ZtRFneMJoX",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=1qNEbhC5FF7vXPuD6LVqHE5ZtRFneMJoX"
            ),
            Card(
                "26", "טלי וגיא ויטנברג", "p",
                videoUrl1 = "https://drive.google.com/uc?export=download&id=1amdBNrUetTqNCSNQmi4UlQdmfMHogF5N",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=1dJK5CIDiGi-XVrnxprJeE532q_iovRPJ",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1uhqg3mzZLk-9adKGXT90TLWT_IsDubPL",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=14X_HtBhI3XqCOQZIRt9atcun2iKHDL3h"
            )
        )
    }
}
