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

        val badge: TextView = view.findViewById(R.id.tvOutfitCount)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)

        return DayViewHolder(view)
    }

    override fun getItemCount(): Int = days.size

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {

        val current = days[position]

        holder.day.text = current.date.dayOfMonth.toString()

        if (current.isToday) {

            holder.day.background =
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.today_circle
                )

            holder.day.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.white
                )
            )

        } else {

            holder.day.background = null

            holder.day.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    if (current.isCurrentMonth)
                        R.color.violet
                    else
                        R.color.calendar_day_disabled
                )
            )

        }

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
        // Previous / next month cells
        if (current.outfits.isEmpty()) {

            holder.image.visibility = View.GONE
            holder.badge.visibility = View.GONE

            if (current.isCurrentMonth) {
                holder.add.visibility = View.VISIBLE
            } else {
                holder.add.visibility = View.GONE
            }

        } else {

            holder.image.visibility = View.VISIBLE
            holder.add.visibility = View.GONE

            holder.image.setImageResource(
                current.outfits.first().imageRes
            )

            if (current.outfits.size > 1) {

                holder.badge.visibility = View.VISIBLE

                holder.badge.text =
                    if (current.outfits.size > 9)
                        "9+"
                    else
                        current.outfits.size.toString()

            } else {

                holder.badge.visibility = View.GONE

            }

        }
    }
}