package com.gggames.hourglass.presentation.endturn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gggames.hourglass.R
import com.gggames.hourglass.model.Card
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.end_turn_card_found_item.view.*

class CardsFoundAdapter(
    val onClick: ((card: Card, playerView: PlayerView, giftText: TextView) -> Unit?)?,
    val onClose: ((PlayerView) -> Unit)?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataSet: List<Card> = emptyList()

    fun setData(cards: List<Card>) {
        dataSet = cards
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.end_turn_card_found_item, parent, false)
        return CardsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    inner class CardsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Card) {
            itemView.name.text = item.name
            itemView.setOnClickListener {
                onClick?.let { it(item, itemView.playerView, itemView.name) }
            }
            itemView.closeButton.setOnClickListener {
                onClose?.let { it(itemView.playerView) }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return (holder as CardsFoundAdapter.CardsViewHolder).bind(dataSet[position])
    }
}

