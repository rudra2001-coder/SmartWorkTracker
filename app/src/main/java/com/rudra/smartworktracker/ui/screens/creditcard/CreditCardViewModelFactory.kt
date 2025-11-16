package com.rudra.smartworktracker.ui.screens.creditcard

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CreditCardViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreditCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreditCardViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
