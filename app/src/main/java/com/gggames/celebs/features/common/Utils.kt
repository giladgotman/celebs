package com.gggames.celebs.features.common

import com.gggames.celebs.common.GAMES_PATH

fun getGamesCollectionPath(baseCollection: String) = "$baseCollection/$GAMES_PATH"

fun getGameCollectionPath(baseCollection: String, gameId: String) = "$baseCollection/$GAMES_PATH/$gameId"