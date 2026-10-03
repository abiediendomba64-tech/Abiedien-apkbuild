package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MasterPT::class,
        MasterProyek::class,
        MasterLahan::class,
        MasterUnit::class,
        MasterPihak::class,
        CoaAccount::class,
        MasterCostCode::class,
        AnggaranProyek::class,
        RevisiAnggaranRecord::class,
        TransaksiKasBankRecord::class,
        PettyCashAdvanceRecord::class,
        PettyCashExpenseRecord::class,
        IntercompanyRecord::class,
        PenjualanUnitContract::class,
        PembayaranCustomerRecord::class,
        PayrollBuruhRecord::class,
        DokumenRegistryRecord::class,
        UserAppEntity::class,
        KontraktorSpkRecord::class,
        AuditLog::class,
        AttendanceRecord::class,
        TaxFilingRecord::class,
        PaymentProofRecord::class,
        MitraInvestorRecord::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dev_properti_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
