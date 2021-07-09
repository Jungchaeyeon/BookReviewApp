package com.jcy.ch12_bookreview.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jcy.ch12_bookreview.dao.HistoryDao
import com.jcy.ch12_bookreview.dao.ReviewDao

@Database(entities = [History::class, Review::class], version =2)
abstract class AppDatabase: RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun reviewDao(): ReviewDao
}

fun getAppDatabase(context:Context) : AppDatabase{

    val migration1_2 = object : Migration(1,2){
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE `REVIEW` (`id` INTEGER, `reivew` TEXT," + "PRIMARY KEY(`id`))")
        }

    }
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "BookSearchDB"
    )
        .addMigrations(migration1_2)
        .build()
}