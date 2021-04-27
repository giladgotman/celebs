package com.gggames.hourglass.presentation.gameon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gggames.hourglass.R
import com.gggames.hourglass.model.Player
import com.gggames.hourglass.model.PlayerTurnState
import com.gggames.hourglass.presentation.common.NameBadge.State
import kotlinx.android.synthetic.main.player_item_layout.view.*

class PlayersAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataSet = PlayersDataSet()

    fun setData(data: PlayersDataSet) {
        dataSet = data
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
        return dataSet.players.size
    }

    inner class PlayersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Player, currentPlayer: Player?, nextPlayer: Player?) {
            val currentOrNextPlayer = currentPlayer ?: nextPlayer

            // if someone is playing show playing and don't show the upnext
            val turnState = if (item.id == currentOrNextPlayer?.id) {
                when (item.id) {
                    currentPlayer?.id -> PlayerTurnState.Playing
                    nextPlayer?.id -> PlayerTurnState.UpNext
                    else -> PlayerTurnState.Idle
                }
            } else {
                PlayerTurnState.Idle
            }
            itemView.nameBadge.state = State(item.name, turnState)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return (holder as PlayersAdapter.PlayersViewHolder).bind(
            dataSet.players[position],
            dataSet.currentPlayer,
            dataSet.nextPlayer
        )
    }
}

data class PlayersDataSet(
    val players: List<Player> = emptyList(),
    val currentPlayer: Player? = null,
    val nextPlayer: Player? = null
)
