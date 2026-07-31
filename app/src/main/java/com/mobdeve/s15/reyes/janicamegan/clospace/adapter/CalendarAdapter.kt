package com.mobdeve.s15.reyes.janicamegan.clospace.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.reyes.janicamegan.clospace.R
import com.mobdeve.s15.reyes.janicamegan.clospace.model.CalendarDay

class CalendarAdapter(

    private val days: List<CalendarDay>

) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val day = view.findViewById<TextView>(R.id.tvDay)

        val outfits =
            view.findViewById<RecyclerView>(R.id.rvOutfits)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)

        return DayViewHolder(view)

    }

    override fun getItemCount() = days.size

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {

        val current = days[position]

        if (current.dayNumber == null) {

            holder.day.text = ""
            holder.outfits.visibility = View.GONE

        } else {

            holder.day.text = current.dayNumber.toString()
            holder.outfits.visibility = View.VISIBLE

            holder.outfits.layoutManager =
                LinearLayoutManager(holder.itemView.context)

            holder.outfits.adapter =
                OutfitAdapter(current.outfits)

        }

        holder.outfits.layoutManager = LinearLayoutManager(
            holder.itemView.context,
            LinearLayoutManager.VERTICAL,
            false
        )

        holder.outfits.isNestedScrollingEnabled = false

        holder.outfits.adapter =
            OutfitAdapter(current.outfits)

    }

}