package com.gggames.celebs.presentation

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.gggames.celebs.R
import com.gggames.celebs.data.FirebaseGamesDataSource
import com.gggames.celebs.data.GamesRepositoryImpl
import com.gggames.celebs.domain.GetGamesUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import com.idagio.app.core.utils.rx.scheduler.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_first.*
import java.lang.StringBuilder

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private val TAG = "gilad"

    private lateinit var getGamesUseCase : GetGamesUseCase

    private val scheduler: BaseSchedulerProvider = SchedulerProvider()

    private val disposables = CompositeDisposable()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getGamesUseCase = GetGamesUseCase(GamesRepositoryImpl(FirebaseGamesDataSource(FirebaseFirestore.getInstance())))
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        fetchGames()
    }

    private fun fetchGames() {
        Log.d(TAG, "fetching games")
        textview_games.text = "fetching games.."
        val gamesObservable = getGamesUseCase()


        val gameNames = StringBuilder()
        gamesObservable.compose(scheduler.applyDefaultSchedulers())
            .subscribe(
                { games ->
                    Log.d(TAG, "fetched games: $games")
                    games.forEach { game ->
                        gameNames.append("${game.name}\n")
                    }
                    textview_games.text = gameNames.toString()
                },
                {
                    textview_games.text = it.message
                }).let { disposables.add(it) }


    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }
}
