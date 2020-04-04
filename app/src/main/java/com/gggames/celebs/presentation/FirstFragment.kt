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
import com.gggames.celebs.data.GamesRepository
import com.gggames.celebs.data.GamesRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private val TAG = "gilad"

    private lateinit var gamesRepo : GamesRepository

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gamesRepo = GamesRepositoryImpl(FirebaseGamesDataSource(FirebaseFirestore.getInstance()))
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        getGames()
    }

    private fun getGames() {
        Log.d(TAG, "fetching games");
        val games = gamesRepo.getGames()
        Log.d(TAG, "fetched games: $games");
    }
}
