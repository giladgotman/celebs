package com.gggames.celebs.presentation.endturn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gggames.celebs.R
import com.gggames.celebs.model.Card
import kotlinx.android.synthetic.main.game_card_layout.view.*


class CardsFoundAdapter(val onClick: (card: Card) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
                onClick(item)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return(holder as CardsFoundAdapter.CardsViewHolder).bind(dataSet[position])
    }
}