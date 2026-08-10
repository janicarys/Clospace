package com.mobdeve.s15.reyes.janicamegan.clospace.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import androidx.recyclerview.widget.RecyclerView

import com.mobdeve.s15.reyes.janicamegan.clospace.ClothingItem
import com.mobdeve.s15.reyes.janicamegan.clospace.R
import com.mobdeve.s15.reyes.janicamegan.clospace.util.ImageDecoder

class GarmentPickerAdapter(
    private val items: List<ClothingItem>,
    private val selectedIds: MutableSet<Int>,
    private val onToggle: (ClothingItem) -> Unit
) : RecyclerView.Adapter<GarmentPickerAdapter.GarmentViewHolder>() {

    class GarmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val image: ImageView = view.findViewById(R.id.imgGarment)

        val check: ImageView = view.findViewById(R.id.imgCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GarmentViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pick_garment, parent, false)

        return GarmentViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: GarmentViewHolder, position: Int) {

        val item = items[position]

        holder.image.setImageBitmap(ImageDecoder.decode(item.imagePath, 220))

        holder.check.visibility =
            if (item.id in selectedIds) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            onToggle(item)
        }
    }
}
