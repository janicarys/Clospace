package com.mobdeve.s15.reyes.janicamegan.clospace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.mobdeve.s15.reyes.janicamegan.clospace.adapter.CalendarAdapter
import com.mobdeve.s15.reyes.janicamegan.clospace.model.CalendarDay
import com.mobdeve.s15.reyes.janicamegan.clospace.util.OutfitRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarFragment : Fragment() {
    private lateinit var backend: BackendRepository
    private lateinit var recycler: RecyclerView
    private lateinit var monthText: TextView
    private var currentMonth = YearMonth.now()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_calendar, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backend = BackendRepository(requireContext())
        recycler = view.findViewById(R.id.rvCalendar)
        monthText = view.findViewById(R.id.tvMonthYear)
        recycler.layoutManager = GridLayoutManager(requireContext(), 7)
        view.findViewById<ImageButton>(R.id.btnPreviousMonth).setOnClickListener { currentMonth = currentMonth.minusMonths(1); loadCalendar() }
        view.findViewById<ImageButton>(R.id.btnNextMonth).setOnClickListener { currentMonth = currentMonth.plusMonths(1); loadCalendar() }
        view.findViewById<MaterialButton>(R.id.btnToday).setOnClickListener { currentMonth = YearMonth.now(); loadCalendar() }
        loadCalendar()
    }

    override fun onResume() { super.onResume(); if (::backend.isInitialized) loadCalendar() }

    private fun loadCalendar() {
        monthText.text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { backend.getCalendarOutfits(currentMonth.toString()) }.onSuccess { byDateOutfits ->
                val previews = withContext(Dispatchers.Default) {
                    byDateOutfits.mapValues { (_, outfits) ->
                        OutfitRenderer.render(outfits.first().placements, 180, 220)
                    }.filterValues { it != null }.mapValues { it.value!! }
                }
                recycler.adapter = CalendarAdapter(generateCalendar(byDateOutfits), previews)
            }
        }
    }

    private fun generateCalendar(byDate: Map<LocalDate, List<OutfitWithItems>>): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val first = currentMonth.atDay(1)
        val start = first.minusDays((first.dayOfWeek.value % 7).toLong())
        for (i in 0 until 42) {
            val date = start.plusDays(i.toLong())
            days += CalendarDay(
                date = date,
                isCurrentMonth = date.month == currentMonth.month,
                isToday = date == LocalDate.now(),
                outfits = byDate[date]?.map { it.outfit } ?: emptyList()
            )
        }
        return days
    }
}
