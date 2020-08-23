package com.gggames.celebs.presentation.endgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gggames.celebs.R
import com.gggames.celebs.model.Card
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.game_over_card_item.view.*

class CardsAdapter(private val onCardInfoClick: (Card, PlayerView, TextView) -> Unit) :
    ListAdapter<Card, CardsAdapter.CardsViewHolder>(TeamDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CardsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.game_over_card_item, parent, false)
        )

    override fun onBindViewHolder(holder: CardsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.game_over_card_item
    }

    inner class CardsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(item: Card) = with(itemView) {
            cardValue.text = item.name
            this.setOnClickListener {
                onCardInfoClick(item, playerView, itemView.cardValue)
            }
        }
    }

    private class TeamDiffUtil : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(
            oldItem: Card,
            newItem: Card
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Card,
            newItem: Card
        ) = oldItem == newItem
    }
}
