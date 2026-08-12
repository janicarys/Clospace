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
public class ClothingDao_Impl(
  __db: RoomDatabase,
) : ClothingDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfClothingItem: EntityInsertAdapter<ClothingItem>

  private val __deleteAdapterOfClothingItem: EntityDeleteOrUpdateAdapter<ClothingItem>

  private val __updateAdapterOfClothingItem: EntityDeleteOrUpdateAdapter<ClothingItem>
  init {
    this.__db = __db
    this.__insertAdapterOfClothingItem = object : EntityInsertAdapter<ClothingItem>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `clothing_items` (`id`,`ownerId`,`name`,`category`,`color`,`material`,`tags`,`imagePath`,`timesWorn`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ClothingItem) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.ownerId.toLong())
        statement.bindText(3, entity.name)
        statement.bindText(4, entity.category)
        statement.bindText(5, entity.color)
        statement.bindText(6, entity.material)
        statement.bindText(7, entity.tags)
        statement.bindText(8, entity.imagePath)
        statement.bindLong(9, entity.timesWorn.toLong())
      }
    }
    this.__deleteAdapterOfClothingItem = object : EntityDeleteOrUpdateAdapter<ClothingItem>() {
      protected override fun createQuery(): String = "DELETE FROM `clothing_items` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ClothingItem) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfClothingItem = object : EntityDeleteOrUpdateAdapter<ClothingItem>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `clothing_items` SET `id` = ?,`ownerId` = ?,`name` = ?,`category` = ?,`color` = ?,`material` = ?,`tags` = ?,`imagePath` = ?,`timesWorn` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ClothingItem) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.ownerId.toLong())
        statement.bindText(3, entity.name)
        statement.bindText(4, entity.category)
        statement.bindText(5, entity.color)
        statement.bindText(6, entity.material)
        statement.bindText(7, entity.tags)
        statement.bindText(8, entity.imagePath)
        statement.bindLong(9, entity.timesWorn.toLong())
        statement.bindLong(10, entity.id.toLong())
      }
    }
  }

  public override suspend fun insert(item: ClothingItem): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfClothingItem.insert(_connection, item)
  }

  public override suspend fun delete(item: ClothingItem): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfClothingItem.handle(_connection, item)
  }

  public override suspend fun update(item: ClothingItem): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfClothingItem.handle(_connection, item)
  }

  public override suspend fun getAll(ownerId: Int): List<ClothingItem> {
    val _sql: String = "SELECT * FROM clothing_items WHERE ownerId = ? ORDER BY id DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, ownerId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOwnerId: Int = getColumnIndexOrThrow(_stmt, "ownerId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfMaterial: Int = getColumnIndexOrThrow(_stmt, "material")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfTimesWorn: Int = getColumnIndexOrThrow(_stmt, "timesWorn")
        val _result: MutableList<ClothingItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: ClothingItem
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpOwnerId: Int
          _tmpOwnerId = _stmt.getLong(_columnIndexOfOwnerId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpColor: String
          _tmpColor = _stmt.getText(_columnIndexOfColor)
          val _tmpMaterial: String
          _tmpMaterial = _stmt.getText(_columnIndexOfMaterial)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpImagePath: String
          _tmpImagePath = _stmt.getText(_columnIndexOfImagePath)
          val _tmpTimesWorn: Int
          _tmpTimesWorn = _stmt.getLong(_columnIndexOfTimesWorn).toInt()
          _item = ClothingItem(_tmpId,_tmpOwnerId,_tmpName,_tmpCategory,_tmpColor,_tmpMaterial,_tmpTags,_tmpImagePath,_tmpTimesWorn)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: Int): ClothingItem? {
    val _sql: String = "SELECT * FROM clothing_items WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOwnerId: Int = getColumnIndexOrThrow(_stmt, "ownerId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfMaterial: Int = getColumnIndexOrThrow(_stmt, "material")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfTimesWorn: Int = getColumnIndexOrThrow(_stmt, "timesWorn")
        val _result: ClothingItem?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpOwnerId: Int
          _tmpOwnerId = _stmt.getLong(_columnIndexOfOwnerId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpColor: String
          _tmpColor = _stmt.getText(_columnIndexOfColor)
          val _tmpMaterial: String
          _tmpMaterial = _stmt.getText(_columnIndexOfMaterial)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpImagePath: String
          _tmpImagePath = _stmt.getText(_columnIndexOfImagePath)
          val _tmpTimesWorn: Int
          _tmpTimesWorn = _stmt.getLong(_columnIndexOfTimesWorn).toInt()
          _result = ClothingItem(_tmpId,_tmpOwnerId,_tmpName,_tmpCategory,_tmpColor,_tmpMaterial,_tmpTags,_tmpImagePath,_tmpTimesWorn)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun search(ownerId: Int, query: String): List<ClothingItem> {
    val _sql: String = """
        |
        |        SELECT * FROM clothing_items
        |        WHERE ownerId = ?
        |        AND (
        |            name LIKE '%' || ? || '%'
        |            OR category LIKE '%' || ? || '%'
        |            OR color LIKE '%' || ? || '%'
        |            OR tags LIKE '%' || ? || '%'
        |        )
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, ownerId.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, query)
        _argIndex = 3
        _stmt.bindText(_argIndex, query)
        _argIndex = 4
        _stmt.bindText(_argIndex, query)
        _argIndex = 5
        _stmt.bindText(_argIndex, query)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOwnerId: Int = getColumnIndexOrThrow(_stmt, "ownerId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfMaterial: Int = getColumnIndexOrThrow(_stmt, "material")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfTimesWorn: Int = getColumnIndexOrThrow(_stmt, "timesWorn")
        val _result: MutableList<ClothingItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: ClothingItem
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpOwnerId: Int
          _tmpOwnerId = _stmt.getLong(_columnIndexOfOwnerId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpColor: String
          _tmpColor = _stmt.getText(_columnIndexOfColor)
          val _tmpMaterial: String
          _tmpMaterial = _stmt.getText(_columnIndexOfMaterial)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpImagePath: String
          _tmpImagePath = _stmt.getText(_columnIndexOfImagePath)
          val _tmpTimesWorn: Int
          _tmpTimesWorn = _stmt.getLong(_columnIndexOfTimesWorn).toInt()
          _item = ClothingItem(_tmpId,_tmpOwnerId,_tmpName,_tmpCategory,_tmpColor,_tmpMaterial,_tmpTags,_tmpImagePath,_tmpTimesWorn)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun filterByCategory(ownerId: Int, category: String): List<ClothingItem> {
    val _sql: String = "SELECT * FROM clothing_items WHERE ownerId = ? AND category = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, ownerId.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, category)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOwnerId: Int = getColumnIndexOrThrow(_stmt, "ownerId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfMaterial: Int = getColumnIndexOrThrow(_stmt, "material")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfTimesWorn: Int = getColumnIndexOrThrow(_stmt, "timesWorn")
        val _result: MutableList<ClothingItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: ClothingItem
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpOwnerId: Int
          _tmpOwnerId = _stmt.getLong(_columnIndexOfOwnerId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpColor: String
          _tmpColor = _stmt.getText(_columnIndexOfColor)
          val _tmpMaterial: String
          _tmpMaterial = _stmt.getText(_columnIndexOfMaterial)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpImagePath: String
          _tmpImagePath = _stmt.getText(_columnIndexOfImagePath)
          val _tmpTimesWorn: Int
          _tmpTimesWorn = _stmt.getLong(_columnIndexOfTimesWorn).toInt()
          _item = ClothingItem(_tmpId,_tmpOwnerId,_tmpName,_tmpCategory,_tmpColor,_tmpMaterial,_tmpTags,_tmpImagePath,_tmpTimesWorn)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun mostWorn(ownerId: Int): ClothingItem? {
    val _sql: String = "SELECT * FROM clothing_items WHERE ownerId = ? ORDER BY timesWorn DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, ownerId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOwnerId: Int = getColumnIndexOrThrow(_stmt, "ownerId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfMaterial: Int = getColumnIndexOrThrow(_stmt, "material")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfTimesWorn: Int = getColumnIndexOrThrow(_stmt, "timesWorn")
        val _result: ClothingItem?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpOwnerId: Int
          _tmpOwnerId = _stmt.getLong(_columnIndexOfOwnerId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpColor: String
          _tmpColor = _stmt.getText(_columnIndexOfColor)
          val _tmpMaterial: String
          _tmpMaterial = _stmt.getText(_columnIndexOfMaterial)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpImagePath: String
          _tmpImagePath = _stmt.getText(_columnIndexOfImagePath)
          val _tmpTimesWorn: Int
          _tmpTimesWorn = _stmt.getLong(_columnIndexOfTimesWorn).toInt()
          _result = ClothingItem(_tmpId,_tmpOwnerId,_tmpName,_tmpCategory,_tmpColor,_tmpMaterial,_tmpTags,_tmpImagePath,_tmpTimesWorn)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun leastWorn(ownerId: Int): ClothingItem? {
    val _sql: String = "SELECT * FROM clothing_items WHERE ownerId = ? ORDER BY timesWorn ASC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, ownerId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOwnerId: Int = getColumnIndexOrThrow(_stmt, "ownerId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfMaterial: Int = getColumnIndexOrThrow(_stmt, "material")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfTimesWorn: Int = getColumnIndexOrThrow(_stmt, "timesWorn")
        val _result: ClothingItem?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpOwnerId: Int
          _tmpOwnerId = _stmt.getLong(_columnIndexOfOwnerId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpColor: String
          _tmpColor = _stmt.getText(_columnIndexOfColor)
          val _tmpMaterial: String
          _tmpMaterial = _stmt.getText(_columnIndexOfMaterial)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpImagePath: String
          _tmpImagePath = _stmt.getText(_columnIndexOfImagePath)
          val _tmpTimesWorn: Int
          _tmpTimesWorn = _stmt.getLong(_columnIndexOfTimesWorn).toInt()
          _result = ClothingItem(_tmpId,_tmpOwnerId,_tmpName,_tmpCategory,_tmpColor,_tmpMaterial,_tmpTags,_tmpImagePath,_tmpTimesWorn)
        } else {
          _result = null
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
