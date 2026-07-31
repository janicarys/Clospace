package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.*

@Dao
interface CalendarDao {

    @Insert
    suspend fun insert(entry: CalendarEntry)

    @Update
    suspend fun update(entry: CalendarEntry)

    @Delete
    suspend fun delete(entry: CalendarEntry)

    @Query("""
        SELECT *
        FROM calendar
        WHERE ownerId = :ownerId
        ORDER BY date
    """)
    suspend fun getAll(ownerId: Int): List<CalendarEntry>

    @Query("""
        SELECT *
        FROM calendar
        WHERE ownerId = :ownerId
        AND date = :date
        LIMIT 1
    """)
    suspend fun getByDate(ownerId: Int, date: String): CalendarEntry?

    // NEW
    @Query("""
        SELECT *
        FROM calendar
        WHERE ownerId = :ownerId
        AND date LIKE :month || '%'
        ORDER BY date
    """)
    suspend fun getByMonth(
        ownerId: Int,
        month: String
    ): List<CalendarEntry>
}

/*call by
    val augustEntries = calendarDao.getByMonth(
    ownerId = 1,
    month = "2026-08"
)
will result
2026-08-03
2026-08-06
2026-08-11

*/


