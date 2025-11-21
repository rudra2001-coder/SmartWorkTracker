package com.rudra.smartworktracker.ui.screens.all_funsion

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AllFunsionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AllFunsionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AllFunsionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
