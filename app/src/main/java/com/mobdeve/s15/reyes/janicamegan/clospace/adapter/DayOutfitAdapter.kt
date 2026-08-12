package com.mobdeve.s15.reyes.janicamegan.clospace.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.reyes.janicamegan.clospace.OutfitWithItems
import com.mobdeve.s15.reyes.janicamegan.clospace.R

class DayOutfitAdapter(
    private val outfits: List<OutfitWithItems>,
    private val previews: Map<Int, Bitmap>,
    private val onClick: (OutfitWithItems) -> Unit,
    private val onDelete: (OutfitWithItems) -> Unit
) : RecyclerView.Adapter<DayOutfitAdapter.DayOutfitViewHolder>() {

    class DayOutfitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: ConstraintLayout = view.findViewById(R.id.cardDayOutfit)
        val image: ImageView = view.findViewById(R.id.imgDayOutfit)
        val delete: ImageButton = view.findViewById(R.id.btnDeleteDayOutfit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayOutfitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_outfit, parent, false)
        return DayOutfitViewHolder(view)
    }

    override fun getItemCount(): Int = outfits.size

    override fun onBindViewHolder(holder: DayOutfitViewHolder, position: Int) {
        val current = outfits[position]

        if (outfits.size == 1) {
            holder.card.updateLayoutParams<ConstraintLayout.LayoutParams> {
                matchConstraintDefaultWidth = ConstraintSet.MATCH_CONSTRAINT_PERCENT
                matchConstraintPercentWidth = 0.5f
            }
        }

        val bitmap = previews[current.outfit.id]
        if (bitmap != null) holder.image.setImageBitmap(bitmap)
        holder.itemView.setOnClickListener { onClick(current) }
        holder.delete.setOnClickListener { onDelete(current) }
    }
}
