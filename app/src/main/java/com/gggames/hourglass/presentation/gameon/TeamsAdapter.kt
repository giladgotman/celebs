package com.gggames.hourglass.presentation.gameon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gggames.hourglass.R
import kotlinx.android.synthetic.main.team_item_layout.view.*

class TeamsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var teams = emptyList<TeamItem>()

    fun setData(teams: List<TeamItem>) {
        this.teams = teams
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.team_item_layout, parent, false)
        view.playersView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = PlayersAdapter()
        }
        return TeamsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TeamsViewHolder).bind(teams[position], position)
    }

    override fun getItemCount() = this.teams.size


    inner class TeamsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: TeamItem, position: Int) {
            itemView.teamName.text = item.name
            itemView.score.text = item.score.toString()
            with (itemView.playersView.adapter as PlayersAdapter) {
                setData(item.playersDataSet)
            }

            val bg = when (position) {
                0 -> R.drawable.ic_score_bg_1
                1 -> R.drawable.ic_score_bg_2
                2 -> R.drawable.ic_score_bg_3
                else -> 0
            }
            itemView.score_bg.setImageResource(bg)

            val teamBg = when (position) {
                0 -> R.drawable.ic_team_name_bg1
                1 -> R.drawable.ic_team_name_bg2
                2 -> R.drawable.ic_team_name_bg3
                else -> 0
            }

            val currentOrNextTeam =
                if (item.playersDataSet.currentPlayer != null) {
                     item.playersDataSet.currentPlayer.team
                } else {
                    item.playersDataSet.nextPlayer?.team
                }

            if (currentOrNextTeam == item.name) {
                itemView.teamName.setBackgroundResource(teamBg)
            } else {
                itemView.teamName.background = null
            }
        }
    }

    data class TeamItem(
        val name: String,
        val playersDataSet: PlayersDataSet,
        val score: Int
    )
}