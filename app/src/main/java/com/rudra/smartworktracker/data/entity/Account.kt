package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountCategory,
    val provider: AccountProvider,
    val accountNumber: String,
    val balance: Double = 0.0,
    val currency: String = "BDT",
    val maxBalance: Double? = null,
    val hasLimit: Boolean = false,
    val dailyTransferLimit: Double? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val nickname: String? = null,
    val linkedGoalId: String? = null,
    val iconColor: Int? = null,
    val notes: String? = null
) {
    fun getEffectiveLimit(): Double? {
        return if (hasLimit && dailyTransferLimit != null) dailyTransferLimit else null
    }

    fun getBalancePercentage(): Float {
        return if (maxBalance != null && maxBalance > 0) {
            (balance.toFloat() / maxBalance.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
}

enum class AccountCategory {
    WALLET,
    BANK,
    MOBILE_BANKING
}

enum class AccountProvider {
    CASH,
    BANK,
    SAVINGS,
    CREDIT_CARD,
    LOAN,
    DBBL,
    CITY_BANK,
    BRAC_BANK,
    BKB,
    SONALI_BANK,
    BKASH,
    NAGAD,
    ROCKET,
    UCASH,
    OTHER
}

fun AccountProvider.displayName(): String = when (this) {
    AccountProvider.CASH -> "Cash"
    AccountProvider.BANK -> "Bank"
    AccountProvider.SAVINGS -> "Savings"
    AccountProvider.CREDIT_CARD -> "Credit Card"
    AccountProvider.LOAN -> "Loan"
    AccountProvider.DBBL -> "DBBL"
    AccountProvider.CITY_BANK -> "City Bank"
    AccountProvider.BRAC_BANK -> "BRAC Bank"
    AccountProvider.BKB -> "BKB"
    AccountProvider.SONALI_BANK -> "Sonali Bank"
    AccountProvider.BKASH -> "bKash"
    AccountProvider.NAGAD -> "Nagad"
    AccountProvider.ROCKET -> "Rocket"
    AccountProvider.UCASH -> "UCash"
    AccountProvider.OTHER -> "Other"
}

fun AccountCategory.displayName(): String = when (this) {
    AccountCategory.WALLET -> "Wallet"
    AccountCategory.BANK -> "Bank"
    AccountCategory.MOBILE_BANKING -> "Mobile Banking"
}

fun AccountProvider.icon(): String = when (this) {
    AccountProvider.CASH -> ""
    AccountProvider.BANK -> ""
    AccountProvider.SAVINGS -> ""
    AccountProvider.CREDIT_CARD -> ""
    AccountProvider.LOAN -> ""
    AccountProvider.DBBL -> ""
    AccountProvider.CITY_BANK -> ""
    AccountProvider.BRAC_BANK -> ""
    AccountProvider.BKB -> ""
    AccountProvider.SONALI_BANK -> ""
    AccountProvider.BKASH -> ""
    AccountProvider.NAGAD -> ""
    AccountProvider.ROCKET -> ""
    AccountProvider.UCASH -> ""
    AccountProvider.OTHER -> ""
}