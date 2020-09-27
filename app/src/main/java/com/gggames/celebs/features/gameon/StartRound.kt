package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.RoundState
import com.gggames.celebs.model.setRoundState
import io.reactivex.Completable
import javax.inject.Inject

class StartRound @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(game: Game): Completable {
        return setGame(
            game
                .setRoundState(RoundState.Started)
        )
    }
}
