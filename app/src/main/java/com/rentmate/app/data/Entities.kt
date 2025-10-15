package com.rentmate.app.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Entity(tableName = "users")
data class User(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val username: String,
	val password: String,
	val email: String = "",
	val isAdmin: Boolean = false
)

@Entity(tableName = "flats")
data class Flat(
	@PrimaryKey val key: Int = 1,
	val name: String = "",
	val address: String = "",
	val dueDay: Int = 10,
	val bkashNumber: String = "01957631102"
)

@Entity(tableName = "expenses")
data class Expense(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val amount: Double,
	val category: String, // "Rent", "Utilities", "Meals", "Extras"
	val description: String = "",
	val date: Long, // timestamp
	val paidBy: Long, // userId who paid
	val participants: String, // comma-separated user IDs who share this expense
	val createdBy: Long, // userId who created entry
	val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface UserDao {
	@Query("SELECT * FROM users WHERE username=:u AND password=:p LIMIT 1")
	suspend fun find(u: String, p: String): User?

	@Query("SELECT * FROM users WHERE id=:id")
	suspend fun getById(id: Long): User?

	@Query("SELECT * FROM users ORDER BY isAdmin DESC, username ASC")
	suspend fun getAll(): List<User>

	@Query("SELECT * FROM users WHERE isAdmin=0 ORDER BY username ASC")
	suspend fun getRoommates(): List<User>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(user: User)

	@Query("SELECT COUNT(*) FROM users")
	suspend fun count(): Int
}

@Dao
interface FlatDao {
	@Query("SELECT * FROM flats WHERE `key`=1")
	suspend fun get(): Flat?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(flat: Flat)
}

@Dao
interface ExpenseDao {
	@Query("SELECT * FROM expenses ORDER BY date DESC")
	suspend fun getAll(): List<Expense>

	@Query("SELECT * FROM expenses WHERE date >= :startTime AND date < :endTime ORDER BY date DESC")
	suspend fun getByDateRange(startTime: Long, endTime: Long): List<Expense>

	@Query("SELECT * FROM expenses WHERE id=:id")
	suspend fun getById(id: Long): Expense?

	@Insert
	suspend fun insert(expense: Expense): Long

	@Update
	suspend fun update(expense: Expense)

	@Delete
	suspend fun delete(expense: Expense)
}

@Database(entities = [User::class, Flat::class, Expense::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
	abstract fun userDao(): UserDao
	abstract fun flatDao(): FlatDao
	abstract fun expenseDao(): ExpenseDao

	companion object {
		@Volatile private var INSTANCE: AppDatabase? = null

		fun getInstance(context: Context): AppDatabase {
			return INSTANCE ?: synchronized(this) {
				val inst = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "rentmate.db")
					.fallbackToDestructiveMigration()
					.build()
				INSTANCE = inst
				seed(inst)
				inst
			}
		}

		private fun seed(db: AppDatabase) {
			CoroutineScope(Dispatchers.IO).launch {
				if (db.userDao().count() == 0) {
					// Seed Admin
					db.userDao().insert(User(username = "Admin", password = "Admin", email = "admin@rentmate.com", isAdmin = true))
					// Seed 3 roommates
					db.userDao().insert(User(username = "Emon", password = "emon123", email = "emon@example.com"))
					db.userDao().insert(User(username = "Bokhtiar", password = "bokhtiar123", email = "bokhtiar@example.com"))
					db.userDao().insert(User(username = "Mahir", password = "mahir123", email = "mahir@example.com"))
				}
				if (db.flatDao().get() == null) {
					db.flatDao().upsert(Flat(name = "Shared Flat", address = "Dhaka, Bangladesh"))
				}
			}
		}
	}
}
