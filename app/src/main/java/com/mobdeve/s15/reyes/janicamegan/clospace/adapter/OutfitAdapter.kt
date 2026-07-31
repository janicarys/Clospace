package com.mobdeve.s15.reyes.janicamegan.clospace.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.reyes.janicamegan.clospace.R
import com.mobdeve.s15.reyes.janicamegan.clospace.model.Outfit

class OutfitAdapter(
    private val outfits: List<Outfit>
) : RecyclerView.Adapter<OutfitAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val image: ImageView =
            view.findViewById(R.id.imgOutfit)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_outfit, parent, false)

        return ViewHolder(view)

    }

    override fun getItemCount() = outfits.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.image.setImageResource(outfits[position].imageRes)

    }

}