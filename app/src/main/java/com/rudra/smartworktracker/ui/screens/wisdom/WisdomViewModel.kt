package com.rudra.smartworktracker.ui.screens.wisdom

import androidx.lifecycle.ViewModel
import com.rudra.smartworktracker.data.repository.WisdomRepository
import com.rudra.smartworktracker.model.Wisdom

class WisdomViewModel : ViewModel() {

    private val repository = WisdomRepository()

    fun getWisdom(): List<Wisdom> {
        return repository.getWisdom()
    }
}
