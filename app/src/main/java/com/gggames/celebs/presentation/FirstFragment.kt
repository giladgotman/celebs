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

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private val TAG = "gilad"

    private lateinit var getGamesUseCase : GetGamesUseCase

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
        Log.d(TAG, "fetching games");
        val games = getGamesUseCase()
        Log.d(TAG, "fetched games: $games");
    }
}
