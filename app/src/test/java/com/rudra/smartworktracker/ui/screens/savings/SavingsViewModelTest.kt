package com.rudra.smartworktracker.ui.screens.savings

import com.rudra.smartworktracker.data.entity.Savings
import com.rudra.smartworktracker.data.repository.SavingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class SavingsViewModelTest {

    private lateinit var viewModel: SavingsViewModel
    private lateinit var savingsRepository: SavingsRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        savingsRepository = mock()
        viewModel = SavingsViewModel(savingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test loadSavingsData updates uiState correctly`() = runTest {
        // Given
        val savings = 1000.0
        val history = listOf(Savings(1, 500.0, 1L), Savings(2, 500.0, 2L))
        whenever(savingsRepository.getSavings()).thenReturn(flowOf(savings))
        whenever(savingsRepository.getSavingsHistory()).thenReturn(flowOf(history))

        // When
        // The view model's init block calls loadSavingsData(), so we just need to observe the state

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals(savings, uiState.savings, 0.0)
        assertEquals(history, uiState.savingsHistory)
    }
}
