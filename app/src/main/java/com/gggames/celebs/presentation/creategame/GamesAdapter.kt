package com.gggames.celebs.presentation.creategame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gggames.celebs.R
import com.gggames.celebs.model.Game
import kotlinx.android.synthetic.main.game_card_layout.view.*

class GamesAdapter(val onClick: (game: Game) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataSet: List<Game> = emptyList()

    fun setData(games: List<Game>) {
        dataSet = games
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.game_card_layout, parent, false)
        return GamesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    inner class GamesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Game) {
            itemView.name.text = item.name
            itemView.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return(holder as GamesViewHolder).bind(dataSet[position])
    }
}
