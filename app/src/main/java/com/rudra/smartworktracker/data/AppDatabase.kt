package com.rudra.smartworktracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rudra.smartworktracker.data.dao.AchievementDao
import com.rudra.smartworktracker.data.dao.CalculationDao
import com.rudra.smartworktracker.data.dao.CreditCardDao
import com.rudra.smartworktracker.data.dao.CreditCardTransactionDao
import com.rudra.smartworktracker.data.dao.DailyJournalDao
import com.rudra.smartworktracker.data.dao.EmiDao
import com.rudra.smartworktracker.data.dao.ExpenseDao
import com.rudra.smartworktracker.data.dao.FinancialTransactionDao
import com.rudra.smartworktracker.data.dao.FocusSessionDao
import com.rudra.smartworktracker.data.dao.HabitDao
import com.rudra.smartworktracker.data.dao.HealthMetricDao
import com.rudra.smartworktracker.data.dao.IncomeDao
import com.rudra.smartworktracker.data.dao.LoanDao
import com.rudra.smartworktracker.data.dao.MonthlyInputDao
import com.rudra.smartworktracker.data.dao.SettingsDao
import com.rudra.smartworktracker.data.dao.SummaryDao
import com.rudra.smartworktracker.data.dao.UserProfileDao
import com.rudra.smartworktracker.data.dao.WorkDayDao
import com.rudra.smartworktracker.data.dao.WorkLogDao
import com.rudra.smartworktracker.data.dao.WorkSessionDao
import com.rudra.smartworktracker.data.entity.Calculation
import com.rudra.smartworktracker.data.entity.CreditCard
import com.rudra.smartworktracker.data.entity.CreditCardTransaction
import com.rudra.smartworktracker.data.entity.Emi
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.data.entity.Loan
import com.rudra.smartworktracker.data.entity.MonthlyInput
import com.rudra.smartworktracker.data.entity.MonthlySummary
import com.rudra.smartworktracker.data.entity.Settings
import com.rudra.smartworktracker.data.entity.UserProfile
import com.rudra.smartworktracker.data.entity.WorkDay
import com.rudra.smartworktracker.data.local.TypeConverters as LocalTypeConverters
import com.rudra.smartworktracker.model.Achievement
import com.rudra.smartworktracker.model.DailyJournal
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.FocusSession
import com.rudra.smartworktracker.model.Habit
import com.rudra.smartworktracker.model.HealthMetric
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkSession

@Database(
    entities = [
        WorkSession::class, 
        Expense::class, 
        Habit::class, 
        FocusSession::class, 
        HealthMetric::class, 
        Achievement::class, 
        DailyJournal::class, 
        WorkLog::class,
        WorkDay::class,
        Settings::class,
        MonthlySummary::class,
        MonthlyInput::class,
        UserProfile::class,
        Income::class,
        Calculation::class,
        FinancialTransaction::class,
        Loan::class,
        Emi::class,
        CreditCard::class,
        CreditCardTransaction::class
    ],
    version = 16, 
    exportSchema = false
)
@TypeConverters(LocalTypeConverters::class, com.rudra.smartworktracker.data.local.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workSessionDao(): WorkSessionDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun habitDao(): HabitDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun healthMetricDao(): HealthMetricDao
    abstract fun achievementDao(): AchievementDao
    abstract fun dailyJournalDao(): DailyJournalDao
    abstract fun workLogDao(): WorkLogDao
    abstract fun workDayDao(): WorkDayDao
    abstract fun settingsDao(): SettingsDao
    abstract fun summaryDao(): SummaryDao
    abstract fun monthlyInputDao(): MonthlyInputDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun incomeDao(): IncomeDao
    abstract fun calculationDao(): CalculationDao
    abstract fun financialTransactionDao(): FinancialTransactionDao
    abstract fun loanDao(): LoanDao
    abstract fun emiDao(): EmiDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun creditCardTransactionDao(): CreditCardTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_work_tracker_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
