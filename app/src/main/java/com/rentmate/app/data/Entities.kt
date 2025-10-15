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

@Dao
interface UserDao {
	@Query("SELECT * FROM users WHERE username=:u AND password=:p LIMIT 1")
	suspend fun find(u: String, p: String): User?

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

@Database(entities = [User::class, Flat::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
	abstract fun userDao(): UserDao
	abstract fun flatDao(): FlatDao

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
					db.userDao().insert(User(username = "Admin", password = "Admin", isAdmin = true))
				}
				if (db.flatDao().get() == null) {
					db.flatDao().upsert(Flat())
				}
			}
		}
	}
}
