package com.gggames.celebs.features.players.domain

import com.gggames.celebs.features.players.data.PlayersRepository
import com.gggames.celebs.model.Player
import io.reactivex.Observable
import javax.inject.Inject

class GetMyPlayer @Inject constructor(

    private val playersRepository: PlayersRepository
) {
    private var cache: Player? = null
    operator fun invoke(gameId: String, userId: String): Observable<Player> {
        val cachedPlayer = playersRepository.getMyPlayer(gameId, userId).doOnNext {
            cache = it
        }
        return if (cache != null) {
            cachedPlayer.startWith(cache)
        } else {
            cachedPlayer.distinctUntilChanged()
        }
    }
}