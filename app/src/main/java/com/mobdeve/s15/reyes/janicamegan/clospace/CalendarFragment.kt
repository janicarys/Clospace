package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.mobdeve.s15.reyes.janicamegan.clospace.adapter.CalendarAdapter
import com.mobdeve.s15.reyes.janicamegan.clospace.model.CalendarDay
import com.mobdeve.s15.reyes.janicamegan.clospace.util.OutfitPreviewCache
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
    private var byDateOutfits: Map<LocalDate, List<OutfitWithItems>> = emptyMap()
    private var pendingDate: LocalDate? = null

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

    override fun onResume() {
        super.onResume()
        if (!::backend.isInitialized) return
        requireActivity().intent.getStringExtra(MainActivity.EXTRA_OPEN_DATE)?.let { raw ->
            runCatching { LocalDate.parse(raw) }.getOrNull()?.let { date ->
                pendingDate = date
                if (YearMonth.from(date) != currentMonth) currentMonth = YearMonth.from(date)
            }
            requireActivity().intent.removeExtra(MainActivity.EXTRA_OPEN_DATE)
        }
        loadCalendar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ClospaceBottomSheets.dismissDaySheet()
    }

    private fun loadCalendar() {
        monthText.text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { backend.getCalendarOutfits(currentMonth.toString()) }.onSuccess {
                bindCalendar(it)
            }
        }
    }

    private suspend fun bindCalendar(result: Map<LocalDate, List<OutfitWithItems>>) {
        byDateOutfits = result
        val previews = withContext(Dispatchers.Default) {
            byDateOutfits.mapValues { (_, outfits) ->
                OutfitRenderer.render(outfits.first().placements, 180, 220)
            }.filterValues { it != null }.mapValues { it.value!! }
        }
        recycler.adapter = CalendarAdapter(generateCalendar(byDateOutfits), previews) { day ->
            showDayOutfits(day.date)
        }
        pendingDate?.let { date ->
            pendingDate = null
            showDayOutfits(date)
        }
    }

    private fun showDayOutfits(date: LocalDate) {
        val outfits = byDateOutfits[date] ?: emptyList()
        viewLifecycleOwner.lifecycleScope.launch {
            val previews = withContext(Dispatchers.Default) {
                outfits.mapNotNull { wrapper ->
                    val bitmap = OutfitRenderer.render(wrapper.placements, 220, 340)
                    if (bitmap != null) wrapper.outfit.id to bitmap else null
                }.toMap()
            }
            ClospaceBottomSheets.showDayOutfits(
                requireContext(),
                date,
                outfits,
                previews,
                onCardClick = { wrapper ->
                    startActivity(
                        Intent(requireContext(), OutfitDetailActivity::class.java)
                            .putExtra(OutfitDetailActivity.EXTRA_OUTFIT_ID, wrapper.outfit.id)
                    )
                },
                onDelete = { wrapper -> confirmRemoveFromDate(date, wrapper.outfit.id) },
                onAddAnother = { addAnotherOutfit(date) }
            )
        }
    }

    private fun addAnotherOutfit(date: LocalDate) {
        startActivity(
            Intent(requireContext(), SelectGarmentsActivity::class.java)
                .putExtra(SelectGarmentsActivity.EXTRA_DATE, date.toString())
        )
    }

    private fun confirmRemoveFromDate(date: LocalDate, outfitId: Int) {
        ClospaceBottomSheets.showConfirm(
            requireContext(),
            R.string.remove_from_date_title,
            R.string.remove_from_date_message,
            R.string.remove
        ) {
            viewLifecycleOwner.lifecycleScope.launch {
                runCatching { backend.removeFromCalendar(outfitId.toLong(), date.toString()) }
                    .onSuccess {
                        OutfitPreviewCache.evict(outfitId)
                        Toast.makeText(requireContext(), R.string.removed_from_date, Toast.LENGTH_SHORT).show()
                        runCatching { backend.getCalendarOutfits(currentMonth.toString()) }
                            .onSuccess { result ->
                                bindCalendar(result)
                                showDayOutfits(date)
                            }
                    }
                    .onFailure { Toast.makeText(requireContext(), it.message ?: "Unable to remove outfit", Toast.LENGTH_LONG).show() }
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
