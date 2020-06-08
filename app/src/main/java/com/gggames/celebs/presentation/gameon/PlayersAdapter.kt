package com.gggames.celebs.presentation.gameon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gggames.celebs.R
import com.gggames.celebs.model.Player
import kotlinx.android.synthetic.main.game_card_layout.view.*


class PlayersAdapter
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataSet: List<Player> = emptyList()

    fun setData(players: List<Player>) {
        dataSet = players
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.player_item_layout, parent, false)
        return PlayersViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    inner class PlayersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Player) {
            itemView.name.text = item.name
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return(holder as PlayersAdapter.PlayersViewHolder).bind(dataSet[position])
    }
}