package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ClospaceDatabase_Impl : ClospaceDatabase() {
  private val _userDao: Lazy<UserDao> = lazy {
    UserDao_Impl(this)
  }

  private val _clothingDao: Lazy<ClothingDao> = lazy {
    ClothingDao_Impl(this)
  }

  private val _outfitDao: Lazy<OutfitDao> = lazy {
    OutfitDao_Impl(this)
  }

  private val _calendarDao: Lazy<CalendarDao> = lazy {
    CalendarDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(6, "3157ca298b50e9b9e1ec8f377ec13fa9", "d20525acac812847f1140b649fe5cbac") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT NOT NULL, `displayName` TEXT NOT NULL, `password` TEXT NOT NULL, `avatar` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `clothing_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ownerId` INTEGER NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, `color` TEXT NOT NULL, `material` TEXT NOT NULL, `tags` TEXT NOT NULL, `imagePath` TEXT NOT NULL, `timesWorn` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `outfits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ownerId` INTEGER NOT NULL, `caption` TEXT, `occasion` TEXT, `tags` TEXT, `plannedDate` TEXT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `outfit_items` (`outfitId` INTEGER NOT NULL, `clothingId` INTEGER NOT NULL, `x` REAL NOT NULL, `y` REAL NOT NULL, `scale` REAL NOT NULL, `layer` INTEGER NOT NULL, PRIMARY KEY(`outfitId`, `clothingId`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `calendar` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ownerId` INTEGER NOT NULL, `date` TEXT NOT NULL, `outfitId` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '3157ca298b50e9b9e1ec8f377ec13fa9')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `users`")
        connection.execSQL("DROP TABLE IF EXISTS `clothing_items`")
        connection.execSQL("DROP TABLE IF EXISTS `outfits`")
        connection.execSQL("DROP TABLE IF EXISTS `outfit_items`")
        connection.execSQL("DROP TABLE IF EXISTS `calendar`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsUsers: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUsers.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("username", TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("displayName", TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("password", TableInfo.Column("password", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("avatar", TableInfo.Column("avatar", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUsers: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesUsers: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoUsers: TableInfo = TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers)
        val _existingUsers: TableInfo = read(connection, "users")
        if (!_infoUsers.equals(_existingUsers)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |users(com.mobdeve.s15.reyes.janicamegan.clospace.User).
              | Expected:
              |""".trimMargin() + _infoUsers + """
              |
              | Found:
              |""".trimMargin() + _existingUsers)
        }
        val _columnsClothingItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsClothingItems.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClothingItems.put("ownerId", TableInfo.Column("ownerId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClothingItems.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClothingItems.put("category", TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClothingItems.put("color", TableInfo.Column("color", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClothingItems.put("material", TableInfo.Column("material", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClothingItems.put("tags", TableInfo.Column("tags", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClothingItems.put("imagePath", TableInfo.Column("imagePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClothingItems.put("timesWorn", TableInfo.Column("timesWorn", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysClothingItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesClothingItems: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoClothingItems: TableInfo = TableInfo("clothing_items", _columnsClothingItems, _foreignKeysClothingItems, _indicesClothingItems)
        val _existingClothingItems: TableInfo = read(connection, "clothing_items")
        if (!_infoClothingItems.equals(_existingClothingItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |clothing_items(com.mobdeve.s15.reyes.janicamegan.clospace.ClothingItem).
              | Expected:
              |""".trimMargin() + _infoClothingItems + """
              |
              | Found:
              |""".trimMargin() + _existingClothingItems)
        }
        val _columnsOutfits: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsOutfits.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOutfits.put("ownerId", TableInfo.Column("ownerId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOutfits.put("caption", TableInfo.Column("caption", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOutfits.put("occasion", TableInfo.Column("occasion", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOutfits.put("tags", TableInfo.Column("tags", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOutfits.put("plannedDate", TableInfo.Column("plannedDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysOutfits: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesOutfits: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoOutfits: TableInfo = TableInfo("outfits", _columnsOutfits, _foreignKeysOutfits, _indicesOutfits)
        val _existingOutfits: TableInfo = read(connection, "outfits")
        if (!_infoOutfits.equals(_existingOutfits)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |outfits(com.mobdeve.s15.reyes.janicamegan.clospace.Outfit).
              | Expected:
              |""".trimMargin() + _infoOutfits + """
              |
              | Found:
              |""".trimMargin() + _existingOutfits)
        }
        val _columnsOutfitItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsOutfitItems.put("outfitId", TableInfo.Column("outfitId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOutfitItems.put("clothingId", TableInfo.Column("clothingId", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOutfitItems.put("x", TableInfo.Column("x", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOutfitItems.put("y", TableInfo.Column("y", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOutfitItems.put("scale", TableInfo.Column("scale", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOutfitItems.put("layer", TableInfo.Column("layer", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysOutfitItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesOutfitItems: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoOutfitItems: TableInfo = TableInfo("outfit_items", _columnsOutfitItems, _foreignKeysOutfitItems, _indicesOutfitItems)
        val _existingOutfitItems: TableInfo = read(connection, "outfit_items")
        if (!_infoOutfitItems.equals(_existingOutfitItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |outfit_items(com.mobdeve.s15.reyes.janicamegan.clospace.OutfitItem).
              | Expected:
              |""".trimMargin() + _infoOutfitItems + """
              |
              | Found:
              |""".trimMargin() + _existingOutfitItems)
        }
        val _columnsCalendar: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCalendar.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendar.put("ownerId", TableInfo.Column("ownerId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendar.put("date", TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendar.put("outfitId", TableInfo.Column("outfitId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCalendar: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCalendar: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCalendar: TableInfo = TableInfo("calendar", _columnsCalendar, _foreignKeysCalendar, _indicesCalendar)
        val _existingCalendar: TableInfo = read(connection, "calendar")
        if (!_infoCalendar.equals(_existingCalendar)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |calendar(com.mobdeve.s15.reyes.janicamegan.clospace.CalendarEntry).
              | Expected:
              |""".trimMargin() + _infoCalendar + """
              |
              | Found:
              |""".trimMargin() + _existingCalendar)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "users", "clothing_items", "outfits", "outfit_items", "calendar")
  }

  public override fun clearAllTables() {
    super.performClear(false, "users", "clothing_items", "outfits", "outfit_items", "calendar")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(UserDao::class, UserDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ClothingDao::class, ClothingDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(OutfitDao::class, OutfitDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CalendarDao::class, CalendarDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun userDao(): UserDao = _userDao.value

  public override fun clothingDao(): ClothingDao = _clothingDao.value

  public override fun outfitDao(): OutfitDao = _outfitDao.value

  public override fun calendarDao(): CalendarDao = _calendarDao.value
}
