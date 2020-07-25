package com.gggames.celebs.features.common

import com.gggames.celebs.common.GAMES_PATH
import com.gggames.celebs.common.USERS_PATH

fun getGamesCollectionPath(baseCollection: String) = "$baseCollection/$GAMES_PATH"

fun getGameCollectionPath(baseCollection: String, gameId: String) = "$baseCollection/$GAMES_PATH/$gameId"

fun getUsersCollectionPath(baseCollection: String) = "$baseCollection/$USERS_PATH"
