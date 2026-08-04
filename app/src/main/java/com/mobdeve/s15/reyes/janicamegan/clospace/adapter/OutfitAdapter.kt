package com.mobdeve.s15.reyes.janicamegan.clospace.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.mobdeve.s15.reyes.janicamegan.clospace.OutfitWithItems
import com.mobdeve.s15.reyes.janicamegan.clospace.R
import com.mobdeve.s15.reyes.janicamegan.clospace.util.OutfitRenderer

class OutfitAdapter(
    private val outfits: List<OutfitWithItems>,
    private val onOpen: (OutfitWithItems) -> Unit
) : RecyclerView.Adapter<OutfitAdapter.OutfitViewHolder>() {

    class OutfitViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val previewFrame: FrameLayout = view.findViewById(R.id.previewFrame)

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

        holder.itemView.setOnClickListener {
            onOpen(current)
        }

        val caption = current.outfit.caption

        if (caption.isNullOrBlank()) {

            holder.tvCaption.visibility = View.GONE

        } else {

            holder.tvCaption.visibility = View.VISIBLE
            holder.tvCaption.text = caption
        }

        val occasion = current.outfit.occasion

        if (occasion.isNullOrBlank()) {

            holder.tvOccasion.visibility = View.GONE

        } else {

            holder.tvOccasion.visibility = View.VISIBLE
            holder.tvOccasion.text = occasion
        }

        val date = current.outfit.plannedDate

        if (date.isNullOrEmpty()) {

            holder.tvDate.visibility = View.GONE

        } else {

            holder.tvDate.visibility = View.VISIBLE
            holder.tvDate.text = date
        }

        holder.previewFrame.removeAllViews()

        val previewRes = current.previewRes

        if (previewRes != null) {

            holder.previewFrame.addView(
                ImageView(context).apply {

                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )

                    scaleType = ImageView.ScaleType.CENTER_CROP

                    setImageResource(previewRes)
                }
            )

        } else {

            val thumbnail =
                OutfitRenderer.render(current.placements, 400, 480)

            if (thumbnail != null) {

                holder.previewFrame.addView(
                    ImageView(context).apply {

                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )

                        scaleType = ImageView.ScaleType.FIT_CENTER

                        setImageBitmap(thumbnail)
                    }
                )
            }
        }
    }
}
