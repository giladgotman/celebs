package com.gggames.celebs.presentation.endgame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.gggames.celebs.R
import com.gggames.celebs.model.Team
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.di.createViewComponent
import com.gggames.celebs.presentation.endgame.GameOverScreenContract.Trigger
import com.gggames.celebs.presentation.endgame.GameOverScreenContract.UiEvent.PressedFinish
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_game_over.*
import kotlinx.android.synthetic.main.team_score_layout.view.*
import javax.inject.Inject

class GameOverFragment : Fragment() {

    @Inject
    lateinit var presenter: GameOverScreenContract.Presenter
    private lateinit var viewComponent: ViewComponent
    private val teamsAndCardsAdapter = TeamsAdapter()

        private val events = PublishSubject.create<GameOverScreenContract.UiEvent>()
    private val disposables = CompositeDisposable()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_over, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewComponent = createViewComponent(this)
        viewComponent.inject(this)

        val gameId: String? = arguments?.getString("gameId")

        presenter.states.subscribe { render(it) }.let { disposables.add(it) }
        presenter.triggers.subscribe { trigger(it) }.let { disposables.add(it) }

        finishButton.setOnClickListener {
            events.onNext(PressedFinish)
        }

        setupRecyclerView()
        presenter.bind(events, gameId!!)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this.context)
        teamsAndCardsRv.layoutManager = layoutManager
        teamsAndCardsRv.itemAnimator = DefaultItemAnimator()
        teamsAndCardsRv.adapter = teamsAndCardsAdapter
    }

    private fun trigger(trigger: Trigger) {
        when (trigger) {
            Trigger.NavigateToGames -> navigateToGames()
        }
    }

    private fun navigateToGames() {
        findNavController().navigate(R.id.action_gameOverFragment_to_GamesFragment)
    }

    private fun render(state: GameOverScreenContract.State) {
        subtitle.text = getString(R.string.game_over_subtitle, state.winningTeam)
        teamsAndCardsAdapter.submitList(state.teams)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
        presenter.unBind()
    }
}

class TeamsAdapter : ListAdapter<Team, TeamsAdapter.TeamViewHolder>(TeamDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TeamViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.team_score_layout, parent, false)
    )

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TeamViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(item: Team) = with(itemView) {
            teamName.text = item.name
            score.text = item.score.toString()
        }

        override fun onClick(v: View?) {

        }
    }

    private class TeamDiffUtil : DiffUtil.ItemCallback<Team>() {
        override fun areItemsTheSame(
            oldItem: Team,
            newItem: Team
        ) = oldItem.name == newItem.name

        override fun areContentsTheSame(
            oldItem: Team,
            newItem: Team
        ) = oldItem == newItem
    }
}