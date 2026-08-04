package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.reyes.janicamegan.clospace.adapter.CalendarAdapter
import com.mobdeve.s15.reyes.janicamegan.clospace.model.CalendarDay
import com.mobdeve.s15.reyes.janicamegan.clospace.model.Outfit
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import android.widget.Button
import com.google.android.material.button.MaterialButton
import java.time.LocalDate

class CalendarActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var monthText: TextView

    private var currentMonth = YearMonth.now()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        recycler = findViewById(R.id.rvCalendar)
        monthText = findViewById(R.id.tvMonthYear)

        recycler.layoutManager = GridLayoutManager(this, 7)

        loadCalendar()

        // ----------------------------
        // Bottom Navigation
        // ----------------------------

        findViewById<LinearLayout>(R.id.navCloset).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navOutfit).setOnClickListener {
            startActivity(Intent(this, OutfitActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }

        // ----------------------------
        // Month Navigation
        // ----------------------------

        findViewById<ImageButton>(R.id.btnPreviousMonth)
            .setOnClickListener {

                currentMonth = currentMonth.minusMonths(1)
                loadCalendar()

            }

        findViewById<ImageButton>(R.id.btnNextMonth)
            .setOnClickListener {

                currentMonth = currentMonth.plusMonths(1)
                loadCalendar()

            }

        findViewById<MaterialButton>(R.id.btnToday)
            .setOnClickListener {

                currentMonth = YearMonth.now()
                loadCalendar()

            }
    }

    private fun loadCalendar() {

        monthText.text =
            currentMonth.format(
                DateTimeFormatter.ofPattern("MMMM yyyy")
            )

        recycler.adapter =
            CalendarAdapter(generateCalendar())
    }

    private fun generateCalendar(): List<CalendarDay> {

        val days = mutableListOf<CalendarDay>()

        // Temporary sample outfit database
        val outfitMap = mapOf(

            LocalDate.of(2026, 7, 31) to mutableListOf(
                Outfit(R.drawable.sample_outfit)
            ),

            LocalDate.of(2026, 8, 3) to mutableListOf(
                Outfit(R.drawable.sample_outfit),
                Outfit(R.drawable.sample_outfit)
            ),

            LocalDate.of(2026, 8, 6) to mutableListOf(
                Outfit(R.drawable.sample_outfit)
            ),

            LocalDate.of(2026, 8, 11) to mutableListOf(
                Outfit(R.drawable.sample_outfit)
            ),

            LocalDate.of(2026, 9, 1) to mutableListOf(
                Outfit(R.drawable.sample_outfit)
            )

        )

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

                    outfits = outfitMap[date]?.toMutableList()
                        ?: mutableListOf()

                )

            )

        }

        return days
    }

}