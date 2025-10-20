package org.secuso.privacyfriendlydicer.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.secuso.privacyfriendlydicer.database.model.DiceMode
import org.secuso.privacyfriendlydicer.databinding.ItemDiceModeBinding

class DiceModeAdapter(diceModes: List<DiceMode>, private val layoutInflater: LayoutInflater): RecyclerView.Adapter<DiceModeAdapter.ViewHolder>() {
    var diceModes: List<DiceMode> = diceModes
        set(value) {
            field = value
            @SuppressLint("NotifyDataSetChanged")
            notifyDataSetChanged()
        }

    var onClick: ((diceMode: DiceMode) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemDiceModeBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mode = diceModes[position]
        holder.binding.diceModeName.setText(mode.name)
        holder.binding.rounds.setText(mode.rounds.toString())
        holder.binding.dices.adapter = DiceAdapter(mode.dices, layoutInflater)
        holder.binding.start.setOnClickListener { onClick?.invoke(mode) }
    }

    override fun getItemCount() = diceModes.size

    fun removeItem(position: Int) {
        diceModes = diceModes.minusElement(diceModes[position])
        notifyItemRemoved(position)
    }

    class ViewHolder(val binding: ItemDiceModeBinding): RecyclerView.ViewHolder(binding.root)
}