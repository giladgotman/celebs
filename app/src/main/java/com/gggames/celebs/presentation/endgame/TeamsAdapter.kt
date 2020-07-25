package com.gggames.celebs.presentation.endgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gggames.celebs.R
import com.gggames.celebs.model.Team
import kotlinx.android.synthetic.main.team_score_layout.view.*

class TeamsAdapter : ListAdapter<Team, TeamsAdapter.TeamViewHolder>(TeamDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TeamViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.team_score_layout, parent, false)
    )

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
      return R.layout.team_score_layout
    }

    inner class TeamViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

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
