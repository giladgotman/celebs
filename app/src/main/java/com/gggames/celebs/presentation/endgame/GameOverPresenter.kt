package com.gggames.celebs.presentation.endgame

import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.presentation.endgame.GameOverScreenContract.State
import com.gggames.celebs.presentation.endgame.GameOverScreenContract.UiEvent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class GameOverPresenter @Inject constructor(
    val gamesRepository: GamesRepository
) {

    private val _states = PublishSubject.create<State>()
    val states: Observable<State> = _states

    fun bind(
        events: Observable<UiEvent>,
        gameId: String
    ) {
        Timber.w("ggg bind, gameId: $gameId")


    }

    fun unBind() {

    }

}
