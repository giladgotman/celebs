package com.gggames.hourglass.presentation.creategame

import android.content.Context
import com.gggames.hourglass.R
import com.gggames.hourglass.core.di.AppContext
import com.gggames.hourglass.features.cards.domain.GetMyCards
import com.gggames.hourglass.features.cards.domain.SetCards
import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.model.Card
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AddCardsPresenter @Inject constructor(
    val getMyCards: GetMyCards,
    val schedulerProvider: BaseSchedulerProvider,
    val gamesRepository: GamesRepository,
    val setCards: SetCards,
    val authenticator: com.gggames.hourglass.core.Authenticator,
    @AppContext val context: Context
) {

    private lateinit var view: View
    private var disposables = CompositeDisposable()

    fun bind(view: View, showSharePopup: Boolean) {
        this.view = view

        getMyCards(authenticator.me!!)
            .doOnSubscribe { view.enableDone(false) }
            .doOnSuccess { view.enableDone(true) }
            .subscribe({ cards ->
                val currentGame = gamesRepository.getCurrentGameBlocking()!!
                val cardsAlreadyFilled = cards.size >= currentGame.celebsCount
                if (cardsAlreadyFilled) {
                    view.navigateToChooseTeam()
                } else {
                    view.showCards(cards, currentGame.celebsCount)
                }

                if (showSharePopup == true) {
                    Observable.timer(1, TimeUnit.SECONDS).subscribe {
                        view.showSharePopup(currentGame.name)
                    }

                }

            }, {
                val errorMessage =
                    if (it is java.lang.IllegalStateException) {
                        it.localizedMessage
                    } else {
                        context.getString(R.string.error_generic)
                    }
                view.showError(errorMessage)
            }).let { disposables.add(it) }
    }

    fun onDoneClicked(cardNames: List<String>) {
        val playerId = authenticator.me!!.id
        val cardList = cardNames.map { editTextToCard(playerId, it) }
        setCards(cardList)
            .doOnSubscribe {
                view.enableDone(false)
            }
            .subscribe({
                view.navigateToChooseTeam()
            }, {
                Timber.e(it, "error while trying to add cards")
                val errorMessage =
                    if (it is java.lang.IllegalStateException) {
                        it.localizedMessage
                    } else {
                        context.getString(R.string.error_generic)
                    }
                view.showError(errorMessage)
                view.enableDone(true)

            }).let {
                disposables.add(it)
            }
    }

    private fun editTextToCard(playerId: String, cardName: String): Card {
        return Card(
                id = "${playerId}.$cardName",
                name = cardName,
                player = playerId
            )
    }


    interface View {
        fun showCards(cards: List<Card>, cardsLimit: Int)
        fun enableDone(enable: Boolean)
        fun navigateToChooseTeam()
        fun showError(errorText: String)
        fun showSharePopup(gameName: String)
    }
}