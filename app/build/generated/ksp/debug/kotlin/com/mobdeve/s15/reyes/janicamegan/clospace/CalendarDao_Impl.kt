package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CalendarDao_Impl(
  __db: RoomDatabase,
) : CalendarDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCalendarEntry: EntityInsertAdapter<CalendarEntry>

  private val __deleteAdapterOfCalendarEntry: EntityDeleteOrUpdateAdapter<CalendarEntry>

  private val __updateAdapterOfCalendarEntry: EntityDeleteOrUpdateAdapter<CalendarEntry>
  init {
    this.__db = __db
    this.__insertAdapterOfCalendarEntry = object : EntityInsertAdapter<CalendarEntry>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `calendar` (`id`,`ownerId`,`date`,`outfitId`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CalendarEntry) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.ownerId.toLong())
        statement.bindText(3, entity.date)
        statement.bindLong(4, entity.outfitId.toLong())
      }
    }
    this.__deleteAdapterOfCalendarEntry = object : EntityDeleteOrUpdateAdapter<CalendarEntry>() {
      protected override fun createQuery(): String = "DELETE FROM `calendar` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: CalendarEntry) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfCalendarEntry = object : EntityDeleteOrUpdateAdapter<CalendarEntry>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `calendar` SET `id` = ?,`ownerId` = ?,`date` = ?,`outfitId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: CalendarEntry) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.ownerId.toLong())
        statement.bindText(3, entity.date)
        statement.bindLong(4, entity.outfitId.toLong())
        statement.bindLong(5, entity.id.toLong())
      }
    }
  }

  public override suspend fun insert(entry: CalendarEntry): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCalendarEntry.insert(_connection, entry)
  }

  public override suspend fun delete(entry: CalendarEntry): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfCalendarEntry.handle(_connection, entry)
  }

  public override suspend fun update(entry: CalendarEntry): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfCalendarEntry.handle(_connection, entry)
  }

  public override suspend fun getAll(ownerId: Int): List<CalendarEntry> {
    val _sql: String = """
        |
        |        SELECT *
        |        FROM calendar
        |        WHERE ownerId = ?
        |        ORDER BY date
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, ownerId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOwnerId: Int = getColumnIndexOrThrow(_stmt, "ownerId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfOutfitId: Int = getColumnIndexOrThrow(_stmt, "outfitId")
        val _result: MutableList<CalendarEntry> = mutableListOf()
        while (_stmt.step()) {
          val _item: CalendarEntry
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpOwnerId: Int
          _tmpOwnerId = _stmt.getLong(_columnIndexOfOwnerId).toInt()
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpOutfitId: Int
          _tmpOutfitId = _stmt.getLong(_columnIndexOfOutfitId).toInt()
          _item = CalendarEntry(_tmpId,_tmpOwnerId,_tmpDate,_tmpOutfitId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByDate(ownerId: Int, date: String): CalendarEntry? {
    val _sql: String = """
        |
        |        SELECT *
        |        FROM calendar
        |        WHERE ownerId = ?
        |        AND date = ?
        |        LIMIT 1
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, ownerId.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOwnerId: Int = getColumnIndexOrThrow(_stmt, "ownerId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfOutfitId: Int = getColumnIndexOrThrow(_stmt, "outfitId")
        val _result: CalendarEntry?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpOwnerId: Int
          _tmpOwnerId = _stmt.getLong(_columnIndexOfOwnerId).toInt()
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpOutfitId: Int
          _tmpOutfitId = _stmt.getLong(_columnIndexOfOutfitId).toInt()
          _result = CalendarEntry(_tmpId,_tmpOwnerId,_tmpDate,_tmpOutfitId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByMonth(ownerId: Int, month: String): List<CalendarEntry> {
    val _sql: String = """
        |
        |        SELECT *
        |        FROM calendar
        |        WHERE ownerId = ?
        |        AND date LIKE ? || '%'
        |        ORDER BY date
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, ownerId.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, month)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOwnerId: Int = getColumnIndexOrThrow(_stmt, "ownerId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfOutfitId: Int = getColumnIndexOrThrow(_stmt, "outfitId")
        val _result: MutableList<CalendarEntry> = mutableListOf()
        while (_stmt.step()) {
          val _item: CalendarEntry
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpOwnerId: Int
          _tmpOwnerId = _stmt.getLong(_columnIndexOfOwnerId).toInt()
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpOutfitId: Int
          _tmpOutfitId = _stmt.getLong(_columnIndexOfOutfitId).toInt()
          _item = CalendarEntry(_tmpId,_tmpOwnerId,_tmpDate,_tmpOutfitId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
