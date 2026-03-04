package com.upro.byyum.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.upro.byyum.data.dao.MonitorDao
import com.upro.byyum.data.dao.MonitorLogDao
import com.upro.byyum.data.entity.Monitor
import com.upro.byyum.data.entity.MonitorLog
import com.upro.byyum.data.entity.MonitorStatus
import com.upro.byyum.data.entity.MonitorType

class Converters {
    @TypeConverter fun fromMonitorType(value: MonitorType): String = value.name
    @TypeConverter fun toMonitorType(value: String): MonitorType = MonitorType.valueOf(value)
    @TypeConverter fun fromMonitorStatus(value: MonitorStatus): String = value.name
    @TypeConverter fun toMonitorStatus(value: String): MonitorStatus = MonitorStatus.valueOf(value)
}

@Database(
    entities = [Monitor::class, MonitorLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun monitorDao(): MonitorDao
    abstract fun monitorLogDao(): MonitorLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yumpro_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
