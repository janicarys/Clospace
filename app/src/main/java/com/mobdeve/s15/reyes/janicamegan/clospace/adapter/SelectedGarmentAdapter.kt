package com.mobdeve.s15.reyes.janicamegan.clospace.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView

import androidx.recyclerview.widget.RecyclerView

import com.mobdeve.s15.reyes.janicamegan.clospace.ClothingItem
import com.mobdeve.s15.reyes.janicamegan.clospace.R
import com.mobdeve.s15.reyes.janicamegan.clospace.util.ImageDecoder

class SelectedGarmentAdapter(
    private val items: MutableList<ClothingItem>,
    private val onRemove: (ClothingItem) -> Unit
) : RecyclerView.Adapter<SelectedGarmentAdapter.SelectedViewHolder>() {

    class SelectedViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val image: ImageView = view.findViewById(R.id.imgSelected)

        val remove: ImageButton = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_garment, parent, false)

        return SelectedViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SelectedViewHolder, position: Int) {

        val item = items[position]

        holder.image.setImageBitmap(ImageDecoder.decode(item.imagePath, 140))

        holder.remove.setOnClickListener {
            onRemove(item)
        }
    }
}
