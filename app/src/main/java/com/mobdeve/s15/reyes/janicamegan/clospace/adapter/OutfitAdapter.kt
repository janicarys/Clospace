package com.mobdeve.s15.reyes.janicamegan.clospace.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.mobdeve.s15.reyes.janicamegan.clospace.OutfitWithItems
import com.mobdeve.s15.reyes.janicamegan.clospace.R
import com.mobdeve.s15.reyes.janicamegan.clospace.util.ImageDecoder

class OutfitAdapter(
    private val outfits: List<OutfitWithItems>
) : RecyclerView.Adapter<OutfitAdapter.OutfitViewHolder>() {

    class OutfitViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val previewStack: LinearLayout = view.findViewById(R.id.previewStack)

        val tvCaption: TextView = view.findViewById(R.id.tvCaption)

        val tvOccasion: TextView = view.findViewById(R.id.tvOccasion)

        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutfitViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_outfit, parent, false)

        return OutfitViewHolder(view)
    }

    override fun getItemCount(): Int = outfits.size

    override fun onBindViewHolder(holder: OutfitViewHolder, position: Int) {

        val current = outfits[position]

        val context = holder.itemView.context

        holder.tvCaption.text = current.outfit.caption

        holder.tvOccasion.text = current.outfit.occasion

        val date = current.outfit.plannedDate

        if (date.isNullOrEmpty()) {

            holder.tvDate.visibility = View.GONE

        } else {

            holder.tvDate.visibility = View.VISIBLE
            holder.tvDate.text = date
        }

        holder.previewStack.removeAllViews()

        current.items.take(3).forEach { item ->

            val image = ImageView(context).apply {

                layoutParams = LinearLayout.LayoutParams(
                    dp(context, 74),
                    dp(context, 96)
                )

                setBackgroundResource(R.drawable.garment_card)
                setPadding(2, 2, 2, 2)

                scaleType = ImageView.ScaleType.CENTER_CROP

                setImageBitmap(ImageDecoder.decode(item.imagePath, 160))
            }

            if (holder.previewStack.childCount > 0) {

                (image.layoutParams as LinearLayout.LayoutParams).leftMargin =
                    -dp(context, 22)
            }

            holder.previewStack.addView(image)
        }
    }

    private fun dp(context: Context, value: Int): Int {

        return (value * context.resources.displayMetrics.density).toInt()
    }
}
