package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        UserProfileEntity::class,
        UserEntity::class,
        ScamAlertEntity::class,
        AppNotificationEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun userDao(): UserDao
    abstract fun scamAlertDao(): ScamAlertDao
    abstract fun notificationDao(): AppNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "promptpay_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            seedDefaultCategoriesIfEmpty(database)
            seedDefaultNotificationsIfEmpty(database)
        }

        suspend fun seedDefaultCategoriesIfEmpty(database: AppDatabase) {
            val existing = database.categoryDao().getCategoriesList()
            if (existing.isEmpty()) {
                val defaultCategories = listOf(
                    CategoryEntity(id = 1, name = "Food & Dining", iconName = "restaurant", tag = "Essential", colorHex = "#FFDAD6", keywords = "starbucks,coffee,food,dining,restaurant,supermarket,simba,grocery"),
                    CategoryEntity(id = 2, name = "Rent & Utilities", iconName = "home", tag = "Fixed", colorHex = "#D7E2FF", keywords = "apartment,rent,utilities,housing,electricity,water,skyline"),
                    CategoryEntity(id = 3, name = "Salary", iconName = "payments", tag = "Income", colorHex = "#FFDDB4", keywords = "salary,income,payroll,deposit"),
                    CategoryEntity(id = 4, name = "Transport", iconName = "directions_car", tag = "Commute", colorHex = "#DAE2FF", keywords = "uber,yego,moto,cab,metro,fuel,shell,bus"),
                    CategoryEntity(id = 5, name = "Entertainment", iconName = "movie", tag = "Lifestyle", colorHex = "#FFB955", keywords = "netflix,cinema,movie,entertainment,spotify"),
                    CategoryEntity(id = 6, name = "Shopping", iconName = "shopping_bag", tag = "Lifestyle", colorHex = "#E1E3E4", keywords = "amazon,mall,shopping,store"),
                    CategoryEntity(id = 7, name = "Health", iconName = "medical_services", tag = "Essential", colorHex = "#FFDAD6", keywords = "pharmacy,hospital,doctor,clinic"),
                    CategoryEntity(id = 8, name = "Bills", iconName = "payments", tag = "Fixed", colorHex = "#EDEEEF", keywords = "bill,tax,fee"),
                    CategoryEntity(id = 9, name = "Transaction Fees", iconName = "payments", tag = "Fixed", colorHex = "#FEF3C7", keywords = "fee,charges,tariff")
                )
                database.categoryDao().insertCategories(defaultCategories)
            }
        }

        suspend fun seedDefaultNotificationsIfEmpty(database: AppDatabase) {
            val existing = database.notificationDao().getAllNotificationsList()
            if (existing.isEmpty()) {
                val defaults = listOf(
                    AppNotificationEntity(
                        id = 1,
                        title = "Pending Review Reminder",
                        message = "You have uncategorized transactions waiting for review.",
                        timestamp = "2 hours ago",
                        isUnread = true,
                        type = "Pending Review",
                        targetRoute = "pending_review"
                    ),
                    AppNotificationEntity(
                        id = 2,
                        title = "Monthly Report Ready",
                        message = "Your financial summary for last month is ready to review.",
                        timestamp = "1 day ago",
                        isUnread = false,
                        type = "Reports",
                        targetRoute = "reports"
                    ),
                    AppNotificationEntity(
                        id = 3,
                        title = "New Login Detected",
                        message = "A new session was authenticated on Pixel 8 Pro.",
                        timestamp = "3 days ago",
                        isUnread = false,
                        type = "Security",
                        targetRoute = "security"
                    )
                )
                for (notif in defaults) {
                    database.notificationDao().insertNotification(notif)
                }
            }
        }
    }
}
