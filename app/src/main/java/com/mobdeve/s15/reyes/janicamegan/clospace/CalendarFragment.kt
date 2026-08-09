package com.mobdeve.s15.reyes.janicamegan.clospace

import android.graphics.Bitmap
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

    private lateinit var sessionManager: SessionManager

    private lateinit var outfitDao: OutfitDao

    private lateinit var recycler: RecyclerView

    private lateinit var monthText: TextView

    private var currentMonth = YearMonth.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        outfitDao = ClospaceDatabase.getDatabase(requireContext()).outfitDao()

        recycler = view.findViewById(R.id.rvCalendar)
        monthText = view.findViewById(R.id.tvMonthYear)

        recycler.layoutManager = GridLayoutManager(requireContext(), 7)

        loadCalendar()

        view.findViewById<ImageButton>(R.id.btnPreviousMonth)
            .setOnClickListener {

                currentMonth = currentMonth.minusMonths(1)
                loadCalendar()
            }

        view.findViewById<ImageButton>(R.id.btnNextMonth)
            .setOnClickListener {

                currentMonth = currentMonth.plusMonths(1)
                loadCalendar()
            }

        view.findViewById<MaterialButton>(R.id.btnToday)
            .setOnClickListener {

                currentMonth = YearMonth.now()
                loadCalendar()
            }
    }

    override fun onResume() {
        super.onResume()
        loadCalendar()
    }

    private fun loadCalendar() {

        val ownerId = sessionManager.getUserId()

        if (ownerId == -1) {
            return
        }

        monthText.text =
            currentMonth.format(
                DateTimeFormatter.ofPattern("MMMM yyyy")
            )

        lifecycleScope.launch {

            val monthPrefix =
                currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

            val planned = outfitDao.getByPlannedMonth(ownerId, monthPrefix)

            val byDate = planned
                .filter { it.plannedDate != null }
                .groupBy { LocalDate.parse(it.plannedDate) }

            val previews = withContext(Dispatchers.Default) {

                byDate.mapValues { (_, outfits) ->

                    val placements = buildPlacements(outfits.first().id)

                    OutfitRenderer.render(placements, 180, 220)
                }.filterValues { it != null }
                    .mapValues { it.value!! }
            }

            recycler.adapter =
                CalendarAdapter(generateCalendar(byDate), previews)
        }
    }

    private suspend fun buildPlacements(outfitId: Int): List<OutfitPlacement> {

        return outfitDao.getOutfitItemsWithClothing(outfitId)
            .map { joined ->

                OutfitPlacement(
                    item = joined.clothing,
                    x = joined.placement.x,
                    y = joined.placement.y,
                    scale = joined.placement.scale,
                    layer = joined.placement.layer
                )
            }
    }

    private fun generateCalendar(
        byDate: Map<LocalDate, List<Outfit>>
    ): List<CalendarDay> {

        val days = mutableListOf<CalendarDay>()

        val firstDayOfMonth = currentMonth.atDay(1)

        val offset = firstDayOfMonth.dayOfWeek.value % 7

        val startDate = firstDayOfMonth.minusDays(offset.toLong())

        for (i in 0 until 42) {

            val date = startDate.plusDays(i.toLong())

            days.add(

                CalendarDay(

                    date = date,

                    isCurrentMonth = date.month == currentMonth.month,

                    isToday = date == LocalDate.now(),

                    outfits = byDate[date] ?: emptyList()
                )
            )
        }

        return days
    }
}