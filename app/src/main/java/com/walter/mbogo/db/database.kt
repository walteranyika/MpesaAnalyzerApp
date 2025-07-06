package com.walter.mbogo.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow // For reactive queries
import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Index
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "money_items", indices = [ Index(value = ["code"], unique = true) ])
data class MoneyItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val person: String?=null,
    @ColumnInfo(name = "code") val code: String,
    val type: String,
    val phone: String?=null,
    val date: Long
)


@Dao
interface MoneyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMoneyItem(item: MoneyItem)

    @Query("SELECT * FROM money_items WHERE type = :transactionType ORDER BY date DESC")
    fun getItemsByType(transactionType: String): Flow<List<MoneyItem>>

    @Delete
    suspend fun deleteMoneyItem(item: MoneyItem)

    @Query("SELECT * FROM money_items WHERE phone = :phoneNumber ORDER BY date DESC")
    fun getItemsByPhone(phoneNumber: String): Flow<List<MoneyItem>>

    @Query("SELECT * FROM money_items WHERE person LIKE '%' || :name || '%' ORDER BY date DESC")
    fun getItemsByName(name: String): Flow<List<MoneyItem>> // Using LIKE for partial name matching

    @Query("SELECT person, type,  phone, SUM(amount) as totalAmount FROM money_items GROUP BY phone, type ORDER BY totalAmount DESC")
    fun getTotalAmountGroupedByPhone(): Flow<List<PhoneTotal>> // Custom data class for result

    // Optional: Get all items (useful for debugging or other features)
    @Query("SELECT * FROM money_items ORDER BY date DESC")
    fun getAllItems(): Flow<List<MoneyItem>>

    // Optional: Get a single item by ID (if needed for updates or specific fetches)
    @Query("SELECT * FROM money_items WHERE id = :itemId")
    fun getItemById(itemId: Int): Flow<MoneyItem?>
}


data class PhoneTotal(
    val phone: String?,
    val person: String?,
    val type: String,
    val totalAmount: Double
)



@Database(entities = [MoneyItem::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moneyDao(): MoneyDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "money_database" // Name of your database file
                )
                    .fallbackToDestructiveMigration() // Use this only during development
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

