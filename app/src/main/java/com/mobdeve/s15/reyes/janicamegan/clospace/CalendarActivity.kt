package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
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

        findViewById<LinearLayout>(R.id.navCloset).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navOutfit).setOnClickListener {
            // TODO
        }

        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }

        findViewById<android.widget.ImageButton>(R.id.btnPreviousMonth)
            .setOnClickListener {

                currentMonth = currentMonth.minusMonths(1)
                loadCalendar()

            }

        findViewById<android.widget.ImageButton>(R.id.btnNextMonth)
            .setOnClickListener {

                currentMonth = currentMonth.plusMonths(1)
                loadCalendar()

            }

    }

    private fun loadCalendar() {

        monthText.text =
            currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

        recycler.adapter = CalendarAdapter(generateCalendar())

    }

    private fun generateCalendar(): List<CalendarDay> {

        val days = mutableListOf<CalendarDay>()

        val firstDay = currentMonth.atDay(1)

        val offset = firstDay.dayOfWeek.value % 7

        repeat(offset) {
            days.add(CalendarDay(null, emptyList()))
        }

        for (day in 1..currentMonth.lengthOfMonth()) {

            val outfits = mutableListOf<Outfit>()

            // Sample data
            when (day) {

                3 -> outfits.add(Outfit(R.drawable.sample_outfit))
                6 -> outfits.add(Outfit(R.drawable.sample_outfit))
                11 -> outfits.add(Outfit(R.drawable.sample_outfit))

            }

            days.add(
                CalendarDay(
                    day,
                    outfits
                )
            )

        }

        return days

    }

}