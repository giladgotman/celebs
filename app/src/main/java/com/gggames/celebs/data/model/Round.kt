package com.gggames.celebs.data.model

enum class Round (name: String) {
    Speaking("Speaking"),
    OneWord("OneWord"),
    Mime("Mime")
}

fun defaultRoundsList() = listOf(Round.Speaking, Round.OneWord, Round.Mime)