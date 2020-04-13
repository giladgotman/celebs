package com.gggames.celebs.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gggames.celebs.R
import com.gggames.celebs.domain.AddCards
import com.gggames.celebs.domain.GetMyCards
import kotlinx.android.synthetic.main.fragment_choose_teams.*
import kotlinx.android.synthetic.main.fragment_choose_teams.view.*
import timber.log.Timber

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ChooseTeamFragment : Fragment() {


    lateinit var addCards: AddCards
    lateinit var getMyCards: GetMyCards

    lateinit var groups: ArrayList<String>

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_teams, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groups = arguments?.getStringArrayList(GROUPS_KEY)!!

        groups.forEachIndexed{index, group->
            when (index) {
                0 -> {
                    teamRadioGroup.radioButtonTeam1.text = group
                }
                1 -> {
                    teamRadioGroup.radioButtonTeam2.text = group
                    teamRadioGroup.radioButtonTeam2.isVisible = true
                }
                2 -> {
                    teamRadioGroup.radioButtonTeam3.text = group
                    teamRadioGroup.radioButtonTeam3.isVisible = true
                }
            }
        }

//        addCards = AddCards(
//            CardsRepositoryImpl(
//                FirebaseCardsDataSource(
//                            "cardsTest1586610436812",
//                    FirebaseFirestore.getInstance()
//                )
//            ),
//            SchedulerProvider()
//        )

        view.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            findNavController().navigate(R.id.action_AddCardsFragment_to_GamesFragment)
        }
        view.findViewById<Button>(R.id.buttonDone).setOnClickListener {
            val selection = teamRadioGroup.checkedRadioButtonId
            val button = view.findViewById<RadioButton>(selection)

            Timber.w("selected team: $selection, team: ${button.text}")

//            getMyCards().subscribe({
//                Timber.w("ggg get cards successfully")
//            },{
//                Timber.e(it,"ggg added cards failed")
//            })
        }


    }
}

const val GROUPS_KEY = "GROUPS_KEY"
