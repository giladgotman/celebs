package com.gggames.celebs.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.celebs.R
import com.gggames.celebs.data.GamesRepositoryImpl
import com.gggames.celebs.data.model.Game
import com.gggames.celebs.data.model.Group
import com.gggames.celebs.data.source.remote.FirebaseGamesDataSource
import com.gggames.celebs.domain.AddGameUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.idagio.app.core.utils.rx.scheduler.SchedulerProvider
import kotlinx.android.synthetic.main.fragment_create_game.*
import timber.log.Timber

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class CreateGameFragment : Fragment() {

    val addGameUseCase = AddGameUseCase(
        GamesRepositoryImpl(
            FirebaseGamesDataSource(
                FirebaseFirestore.getInstance()
            )
        ),
        SchedulerProvider()
    )
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            findNavController().navigate(R.id.action_CreateGame_to_GamesFragment)
        }

        view.findViewById<Button>(R.id.buttonDone).setOnClickListener {
            val now = System.currentTimeMillis()
            val cardsCount = cardsAmount.editText?.text.toString().toInt()
            val groupList = mutableListOf<Group>()
            if (groupName1.editText?.text?.isNotEmpty() == true) {
                groupList.add(Group(groupName1.editText?.text.toString(), emptyList()))
            }
            if (groupName2.editText?.text?.isNotEmpty() == true) {
                groupList.add(Group(groupName2.editText?.text.toString(), emptyList()))
            }
            if (groupName3.editText?.text?.isNotEmpty() == true) {
                groupList.add(Group(groupName3.editText?.text.toString(), emptyList()))
            }
            val game = Game("${gameName.editText?.text}$now", gameName.editText?.text.toString(), now, cardsCount, groupList)

            addGameUseCase(game)
                .subscribe(
                    {
                        Timber.i("gilad game added: ${game.id}")
                        val args = Bundle()
                        args.putString(GAME_ID, game.id)
                        findNavController().navigate(R.id.action_CreateGameFragment_to_AddCardsFragment, args)
                    }, {
                        Timber.e(it,"gilad game added failed. ${it.localizedMessage}")
                    })
        }
    }
}
