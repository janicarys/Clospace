package com.mobdeve.s15.reyes.janicamegan.clospace.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.reyes.janicamegan.clospace.R
import com.mobdeve.s15.reyes.janicamegan.clospace.model.CalendarDay
import androidx.core.content.ContextCompat

class CalendarAdapter(
    private val days: List<CalendarDay>
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val day: TextView = view.findViewById(R.id.tvDay)
        val image: ImageView = view.findViewById(R.id.imgOutfit)
        val add: ImageView = view.findViewById(R.id.imgAdd)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)

        return DayViewHolder(view)
    }

    override fun getItemCount(): Int = days.size

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {

        val current = days[position]

        holder.day.text = current.dayNumber.toString()

        if (current.isCurrentMonth) {

            holder.day.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.violet
                )
            )
            holder.itemView.alpha = 1f

        } else {

            holder.day.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.calendar_day_disabled

                )
            )

        }

        // No outfit yet
        if (current.outfits.isEmpty()) {

            holder.image.visibility = View.GONE
            holder.add.visibility = View.VISIBLE

        } else {

            holder.add.visibility = View.GONE
            holder.image.visibility = View.VISIBLE

            holder.image.setImageResource(
                current.outfits.first().imageRes
            )
        }
    }
}