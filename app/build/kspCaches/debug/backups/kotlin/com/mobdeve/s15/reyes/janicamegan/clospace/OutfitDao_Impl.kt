package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class OutfitDao_Impl(
  __db: RoomDatabase,
) : OutfitDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfOutfit: EntityInsertAdapter<Outfit>

  private val __insertAdapterOfOutfitItem: EntityInsertAdapter<OutfitItem>

  private val __deleteAdapterOfOutfit: EntityDeleteOrUpdateAdapter<Outfit>

  private val __updateAdapterOfOutfit: EntityDeleteOrUpdateAdapter<Outfit>
  init {
    this.__db = __db
    this.__insertAdapterOfOutfit = object : EntityInsertAdapter<Outfit>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `outfits` (`id`,`ownerId`,`caption`,`occasion`,`tags`,`plannedDate`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Outfit) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.ownerId.toLong())
        val _tmpCaption: String? = entity.caption
        if (_tmpCaption == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpCaption)
        }
        val _tmpOccasion: String? = entity.occasion
        if (_tmpOccasion == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpOccasion)
        }
        val _tmpTags: String? = entity.tags
        if (_tmpTags == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpTags)
        }
        val _tmpPlannedDate: String? = entity.plannedDate
        if (_tmpPlannedDate == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpPlannedDate)
        }
      }
    }
    this.__insertAdapterOfOutfitItem = object : EntityInsertAdapter<OutfitItem>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `outfit_items` (`outfitId`,`clothingId`,`x`,`y`,`scale`,`layer`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: OutfitItem) {
        statement.bindLong(1, entity.outfitId.toLong())
        statement.bindLong(2, entity.clothingId.toLong())
        statement.bindDouble(3, entity.x.toDouble())
        statement.bindDouble(4, entity.y.toDouble())
        statement.bindDouble(5, entity.scale.toDouble())
        statement.bindLong(6, entity.layer.toLong())
      }
    }
    this.__deleteAdapterOfOutfit = object : EntityDeleteOrUpdateAdapter<Outfit>() {
      protected override fun createQuery(): String = "DELETE FROM `outfits` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Outfit) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfOutfit = object : EntityDeleteOrUpdateAdapter<Outfit>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `outfits` SET `id` = ?,`ownerId` = ?,`caption` = ?,`occasion` = ?,`tags` = ?,`plannedDate` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Outfit) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.ownerId.toLong())
        val _tmpCaption: String? = entity.caption
        if (_tmpCaption == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpCaption)
        }
        val _tmpOccasion: String? = entity.occasion
        if (_tmpOccasion == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpOccasion)
        }
        val _tmpTags: String? = entity.tags
        if (_tmpTags == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpTags)
        }
        val _tmpPlannedDate: String? = entity.plannedDate
        if (_tmpPlannedDate == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpPlannedDate)
        }
        statement.bindLong(7, entity.id.toLong())
      }
    }
  }

  public override suspend fun insert(outfit: Outfit): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfOutfit.insertAndReturnId(_connection, outfit)
    _result
  }

  public override suspend fun insertOutfitItems(items: List<OutfitItem>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfOutfitItem.insert(_connection, items)
  }

  public override suspend fun delete(outfit: Outfit): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfOutfit.handle(_connection, outfit)
  }

  public override suspend fun update(outfit: Outfit): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfOutfit.handle(_connection, outfit)
  }

  public override suspend fun getAll(ownerId: Int): List<Outfit> {
    val _sql: String = "SELECT * FROM outfits WHERE ownerId = ? ORDER BY id DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, ownerId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOwnerId: Int = getColumnIndexOrThrow(_stmt, "ownerId")
        val _columnIndexOfCaption: Int = getColumnIndexOrThrow(_stmt, "caption")
        val _columnIndexOfOccasion: Int = getColumnIndexOrThrow(_stmt, "occasion")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfPlannedDate: Int = getColumnIndexOrThrow(_stmt, "plannedDate")
        val _result: MutableList<Outfit> = mutableListOf()
        while (_stmt.step()) {
          val _item: Outfit
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpOwnerId: Int
          _tmpOwnerId = _stmt.getLong(_columnIndexOfOwnerId).toInt()
          val _tmpCaption: String?
          if (_stmt.isNull(_columnIndexOfCaption)) {
            _tmpCaption = null
          } else {
            _tmpCaption = _stmt.getText(_columnIndexOfCaption)
          }
          val _tmpOccasion: String?
          if (_stmt.isNull(_columnIndexOfOccasion)) {
            _tmpOccasion = null
          } else {
            _tmpOccasion = _stmt.getText(_columnIndexOfOccasion)
          }
          val _tmpTags: String?
          if (_stmt.isNull(_columnIndexOfTags)) {
            _tmpTags = null
          } else {
            _tmpTags = _stmt.getText(_columnIndexOfTags)
          }
          val _tmpPlannedDate: String?
          if (_stmt.isNull(_columnIndexOfPlannedDate)) {
            _tmpPlannedDate = null
          } else {
            _tmpPlannedDate = _stmt.getText(_columnIndexOfPlannedDate)
          }
          _item = Outfit(_tmpId,_tmpOwnerId,_tmpCaption,_tmpOccasion,_tmpTags,_tmpPlannedDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: Int): Outfit? {
    val _sql: String = "SELECT * FROM outfits WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOwnerId: Int = getColumnIndexOrThrow(_stmt, "ownerId")
        val _columnIndexOfCaption: Int = getColumnIndexOrThrow(_stmt, "caption")
        val _columnIndexOfOccasion: Int = getColumnIndexOrThrow(_stmt, "occasion")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfPlannedDate: Int = getColumnIndexOrThrow(_stmt, "plannedDate")
        val _result: Outfit?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpOwnerId: Int
          _tmpOwnerId = _stmt.getLong(_columnIndexOfOwnerId).toInt()
          val _tmpCaption: String?
          if (_stmt.isNull(_columnIndexOfCaption)) {
            _tmpCaption = null
          } else {
            _tmpCaption = _stmt.getText(_columnIndexOfCaption)
          }
          val _tmpOccasion: String?
          if (_stmt.isNull(_columnIndexOfOccasion)) {
            _tmpOccasion = null
          } else {
            _tmpOccasion = _stmt.getText(_columnIndexOfOccasion)
          }
          val _tmpTags: String?
          if (_stmt.isNull(_columnIndexOfTags)) {
            _tmpTags = null
          } else {
            _tmpTags = _stmt.getText(_columnIndexOfTags)
          }
          val _tmpPlannedDate: String?
          if (_stmt.isNull(_columnIndexOfPlannedDate)) {
            _tmpPlannedDate = null
          } else {
            _tmpPlannedDate = _stmt.getText(_columnIndexOfPlannedDate)
          }
          _result = Outfit(_tmpId,_tmpOwnerId,_tmpCaption,_tmpOccasion,_tmpTags,_tmpPlannedDate)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getOutfitsForClothing(clothingId: Int): List<Outfit> {
    val _sql: String = """
        |
        |        SELECT outfits.* FROM outfits
        |        INNER JOIN outfit_items ON outfit_items.outfitId = outfits.id
        |        WHERE outfit_items.clothingId = ?
        |        ORDER BY outfits.id DESC
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, clothingId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOwnerId: Int = getColumnIndexOrThrow(_stmt, "ownerId")
        val _columnIndexOfCaption: Int = getColumnIndexOrThrow(_stmt, "caption")
        val _columnIndexOfOccasion: Int = getColumnIndexOrThrow(_stmt, "occasion")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfPlannedDate: Int = getColumnIndexOrThrow(_stmt, "plannedDate")
        val _result: MutableList<Outfit> = mutableListOf()
        while (_stmt.step()) {
          val _item: Outfit
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpOwnerId: Int
          _tmpOwnerId = _stmt.getLong(_columnIndexOfOwnerId).toInt()
          val _tmpCaption: String?
          if (_stmt.isNull(_columnIndexOfCaption)) {
            _tmpCaption = null
          } else {
            _tmpCaption = _stmt.getText(_columnIndexOfCaption)
          }
          val _tmpOccasion: String?
          if (_stmt.isNull(_columnIndexOfOccasion)) {
            _tmpOccasion = null
          } else {
            _tmpOccasion = _stmt.getText(_columnIndexOfOccasion)
          }
          val _tmpTags: String?
          if (_stmt.isNull(_columnIndexOfTags)) {
            _tmpTags = null
          } else {
            _tmpTags = _stmt.getText(_columnIndexOfTags)
          }
          val _tmpPlannedDate: String?
          if (_stmt.isNull(_columnIndexOfPlannedDate)) {
            _tmpPlannedDate = null
          } else {
            _tmpPlannedDate = _stmt.getText(_columnIndexOfPlannedDate)
          }
          _item = Outfit(_tmpId,_tmpOwnerId,_tmpCaption,_tmpOccasion,_tmpTags,_tmpPlannedDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByPlannedMonth(ownerId: Int, monthPrefix: String): List<Outfit> {
    val _sql: String = """
        |
        |        SELECT * FROM outfits
        |        WHERE ownerId = ?
        |        AND plannedDate IS NOT NULL
        |        AND plannedDate LIKE ? || '%'
        |        ORDER BY plannedDate
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, ownerId.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, monthPrefix)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOwnerId: Int = getColumnIndexOrThrow(_stmt, "ownerId")
        val _columnIndexOfCaption: Int = getColumnIndexOrThrow(_stmt, "caption")
        val _columnIndexOfOccasion: Int = getColumnIndexOrThrow(_stmt, "occasion")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfPlannedDate: Int = getColumnIndexOrThrow(_stmt, "plannedDate")
        val _result: MutableList<Outfit> = mutableListOf()
        while (_stmt.step()) {
          val _item: Outfit
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpOwnerId: Int
          _tmpOwnerId = _stmt.getLong(_columnIndexOfOwnerId).toInt()
          val _tmpCaption: String?
          if (_stmt.isNull(_columnIndexOfCaption)) {
            _tmpCaption = null
          } else {
            _tmpCaption = _stmt.getText(_columnIndexOfCaption)
          }
          val _tmpOccasion: String?
          if (_stmt.isNull(_columnIndexOfOccasion)) {
            _tmpOccasion = null
          } else {
            _tmpOccasion = _stmt.getText(_columnIndexOfOccasion)
          }
          val _tmpTags: String?
          if (_stmt.isNull(_columnIndexOfTags)) {
            _tmpTags = null
          } else {
            _tmpTags = _stmt.getText(_columnIndexOfTags)
          }
          val _tmpPlannedDate: String?
          if (_stmt.isNull(_columnIndexOfPlannedDate)) {
            _tmpPlannedDate = null
          } else {
            _tmpPlannedDate = _stmt.getText(_columnIndexOfPlannedDate)
          }
          _item = Outfit(_tmpId,_tmpOwnerId,_tmpCaption,_tmpOccasion,_tmpTags,_tmpPlannedDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getOutfitItemsWithClothing(outfitId: Int): List<OutfitItemWithClothing> {
    val _sql: String = """
        |
        |        SELECT * FROM outfit_items
        |        INNER JOIN clothing_items ON clothing_items.id = outfit_items.clothingId
        |        WHERE outfit_items.outfitId = ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, outfitId.toLong())
        val _columnIndexOfOutfitId: Int = getColumnIndexOrThrow(_stmt, "outfitId")
        val _columnIndexOfClothingId: Int = getColumnIndexOrThrow(_stmt, "clothingId")
        val _columnIndexOfX: Int = getColumnIndexOrThrow(_stmt, "x")
        val _columnIndexOfY: Int = getColumnIndexOrThrow(_stmt, "y")
        val _columnIndexOfScale: Int = getColumnIndexOrThrow(_stmt, "scale")
        val _columnIndexOfLayer: Int = getColumnIndexOrThrow(_stmt, "layer")
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOwnerId: Int = getColumnIndexOrThrow(_stmt, "ownerId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfMaterial: Int = getColumnIndexOrThrow(_stmt, "material")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfTimesWorn: Int = getColumnIndexOrThrow(_stmt, "timesWorn")
        val _result: MutableList<OutfitItemWithClothing> = mutableListOf()
        while (_stmt.step()) {
          val _item: OutfitItemWithClothing
          val _tmpPlacement: OutfitItem
          val _tmpOutfitId: Int
          _tmpOutfitId = _stmt.getLong(_columnIndexOfOutfitId).toInt()
          val _tmpClothingId: Int
          _tmpClothingId = _stmt.getLong(_columnIndexOfClothingId).toInt()
          val _tmpX: Float
          _tmpX = _stmt.getDouble(_columnIndexOfX).toFloat()
          val _tmpY: Float
          _tmpY = _stmt.getDouble(_columnIndexOfY).toFloat()
          val _tmpScale: Float
          _tmpScale = _stmt.getDouble(_columnIndexOfScale).toFloat()
          val _tmpLayer: Int
          _tmpLayer = _stmt.getLong(_columnIndexOfLayer).toInt()
          _tmpPlacement = OutfitItem(_tmpOutfitId,_tmpClothingId,_tmpX,_tmpY,_tmpScale,_tmpLayer)
          val _tmpClothing: ClothingItem
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
          _tmpClothing = ClothingItem(_tmpId,_tmpOwnerId,_tmpName,_tmpCategory,_tmpColor,_tmpMaterial,_tmpTags,_tmpImagePath,_tmpTimesWorn)
          _item = OutfitItemWithClothing(_tmpPlacement,_tmpClothing)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteOutfitItems(outfitId: Int) {
    val _sql: String = "DELETE FROM outfit_items WHERE outfitId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, outfitId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteOutfitItemsForClothing(clothingId: Int) {
    val _sql: String = "DELETE FROM outfit_items WHERE clothingId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, clothingId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
