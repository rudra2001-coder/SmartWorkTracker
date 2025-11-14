package com.rudra.smartworktracker.ui.screens.all_funsion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AllFunsionViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AllFunsionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AllFunsionViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
