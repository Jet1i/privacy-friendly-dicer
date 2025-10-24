package org.secuso.privacyfriendlydicer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.secuso.privacyfriendlydicer.database.dao.DiceModeDao
import org.secuso.privacyfriendlydicer.database.model.DiceMode
import java.io.File

@Database(
    entities = [ DiceMode::class ],
    version = DicerDatabase.VERSION
)
@TypeConverters(Converters::class)
abstract class DicerDatabase: RoomDatabase() {

    abstract fun diceModeDao(): DiceModeDao

    companion object {
        const val VERSION = 1
        const val DATABASE_NAME = "dicer"

        private var _instance: DicerDatabase? = null

        private val roomCallback: RoomDatabase.Callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }

        private val migrations: List<Migration> = listOf()

        fun getInstance(context: Context): DicerDatabase {
            return getInstance(context, DATABASE_NAME)
        }

        fun getInstance(context: Context, databaseName: String): DicerDatabase {
            if (_instance == null || DATABASE_NAME != databaseName) {
                _instance = Room.databaseBuilder(context.applicationContext, DicerDatabase::class.java, databaseName)
                    .allowMainThreadQueries()
                    .addMigrations(migrations)
                    .addCallback(roomCallback)
                    .build()
            }
            return _instance!!
        }

        fun getInstance(context: Context, databaseName: String, file: File): DicerDatabase {
            if (_instance == null) {
                _instance = Room.databaseBuilder(context.applicationContext, DicerDatabase::class.java, databaseName)
                    .createFromFile(file)
                    .allowMainThreadQueries()
                    .addMigrations(migrations)
                    .addCallback(roomCallback)
                    .build()
            }
            return _instance!!
        }
    }
}

fun <T: RoomDatabase> RoomDatabase.Builder<T>.addMigrations(migrations: List<Migration>): RoomDatabase.Builder<T> {
    migrations.forEach { this.addMigrations(it) }
    return this
}