package com.gggames.hourglass.presentation.debug

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gggames.hourglass.R
import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.features.games.domain.GetMyGames
import com.gggames.hourglass.features.games.domain.SetGame
import com.gggames.hourglass.model.Game
import com.gggames.hourglass.presentation.di.createViewComponent
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_debug.*
import timber.log.Timber
import javax.inject.Inject

class DebugActivity : AppCompatActivity() {


    @Inject
    lateinit var gamesRepository: GamesRepository

    @Inject
    lateinit var getMyGames: GetMyGames

    @Inject
    lateinit var schedulerProvider: BaseSchedulerProvider

    @Inject
    lateinit var setGame: SetGame

    private var games = emptyList<Game>()

    private val disposables = CompositeDisposable()
    private var changeCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        createViewComponent(this).inject(this)

        getMyGames()
            .compose(schedulerProvider.applyDefault())
            .subscribe({
                games = it
                val game = it.firstOrNull()


                game?.let {
                    gameName.text = game.name
                    Timber.d(" game name: ${it.name}")
                }
            }, {
                Timber.e(it, "error while fetching games")
            }).let { disposables.add(it) }

        changeGameButton.setOnClickListener {
            val game = games.firstOrNull()
            game?.let {
                val name = it.name + "-${changeCounter++}"
                setGame(game.copy(name = name))
            }

        }

    }
}