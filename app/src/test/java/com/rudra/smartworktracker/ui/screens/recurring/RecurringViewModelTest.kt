package com.rudra.smartworktracker.ui.screens.recurring

import com.rudra.smartworktracker.data.entity.RecurringFrequency
import com.rudra.smartworktracker.data.entity.RecurringRule
import com.rudra.smartworktracker.data.entity.RecurringTransaction
import com.rudra.smartworktracker.data.entity.RecurringTransactionStatus
import com.rudra.smartworktracker.data.entity.TransactionType
import com.rudra.smartworktracker.data.repository.RecurringRepository
import com.rudra.smartworktracker.engine.RecurringEngine
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class RecurringViewModelTest {

    private lateinit var viewModel: RecurringViewModel
    private lateinit var recurringRepository: RecurringRepository
    private lateinit var recurringEngine: RecurringEngine
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        recurringRepository = mock()
        recurringEngine = mock()
        viewModel = RecurringViewModel(recurringRepository, recurringEngine)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty rules`() = runTest {
        whenever(recurringRepository.getAllRules()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getActiveRules()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getAllTransactions()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getTransactionsBetweenDates(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(flowOf(emptyList()))

        viewModel = RecurringViewModel(recurringRepository, recurringEngine)

        val state = viewModel.uiState.first()
        assertTrue(state.rules.isEmpty())
        assertEquals(0, state.activeRulesCount)
    }

    @Test
    fun `toggleMultiSelect toggles multi-select mode`() = runTest {
        whenever(recurringRepository.getAllRules()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getActiveRules()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getAllTransactions()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getTransactionsBetweenDates(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(flowOf(emptyList()))

        viewModel = RecurringViewModel(recurringRepository, recurringEngine)

        assertFalse(viewModel.uiState.value.isMultiSelectMode)
        viewModel.toggleMultiSelect()
        assertTrue(viewModel.uiState.value.isMultiSelectMode)
        viewModel.toggleMultiSelect()
        assertFalse(viewModel.uiState.value.isMultiSelectMode)
    }

    @Test
    fun `toggleRuleSelection adds and removes rule IDs`() = runTest {
        whenever(recurringRepository.getAllRules()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getActiveRules()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getAllTransactions()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getTransactionsBetweenDates(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(flowOf(emptyList()))

        viewModel = RecurringViewModel(recurringRepository, recurringEngine)

        viewModel.toggleRuleSelection(1L)
        assertTrue(viewModel.uiState.value.selectedRuleIds.contains(1L))

        viewModel.toggleRuleSelection(2L)
        assertTrue(viewModel.uiState.value.selectedRuleIds.contains(2L))

        viewModel.toggleRuleSelection(1L)
        assertFalse(viewModel.uiState.value.selectedRuleIds.contains(1L))
    }

    @Test
    fun `deleteSelectedRules deletes rules and exits multi-select`() = runTest {
        val rule1 = createTestRule(1L, "Rule 1")
        val rule2 = createTestRule(2L, "Rule 2")

        whenever(recurringRepository.getAllRules()).thenReturn(flowOf(listOf(rule1, rule2)))
        whenever(recurringRepository.getActiveRules()).thenReturn(flowOf(listOf(rule1, rule2)))
        whenever(recurringRepository.getAllTransactions()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getTransactionsBetweenDates(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(flowOf(emptyList()))

        viewModel = RecurringViewModel(recurringRepository, recurringEngine)
        viewModel.toggleMultiSelect()
        viewModel.toggleRuleSelection(1L)
        viewModel.deleteSelectedRules()

        verify(recurringRepository).deleteRule(rule1)
    }

    @Test
    fun `snoozeTransaction calls repository with new date`() = runTest {
        val transaction = createTestTransaction(1L, scheduledDate = 1000L)

        whenever(recurringRepository.getAllRules()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getActiveRules()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getAllTransactions()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getTransactionsBetweenDates(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(flowOf(emptyList()))

        viewModel = RecurringViewModel(recurringRepository, recurringEngine)
        viewModel.snoozeTransaction(transaction, days = 2)

        val expectedNewDate = 1000L + (2L * 24 * 60 * 60 * 1000)
        verify(recurringRepository).snoozeTransaction(1L, expectedNewDate)
    }

    @Test
    fun `confirmTransaction updates transaction as confirmed`() = runTest {
        val transaction = createTestTransaction(1L)

        whenever(recurringRepository.getAllRules()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getActiveRules()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getAllTransactions()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getTransactionsBetweenDates(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(flowOf(emptyList()))

        viewModel = RecurringViewModel(recurringRepository, recurringEngine)
        viewModel.confirmTransaction(transaction)

        verify(recurringRepository).updateTransaction(
            transaction.copy(isConfirmed = true, status = RecurringTransactionStatus.CONFIRMED)
        )
    }

    @Test
    fun `updateSearchQuery triggers filterRules`() = runTest {
        whenever(recurringRepository.getAllRules()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getActiveRules()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getAllTransactions()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getTransactionsBetweenDates(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.searchRules("test")).thenReturn(flowOf(emptyList()))

        viewModel = RecurringViewModel(recurringRepository, recurringEngine)
        viewModel.updateSearchQuery("test")

        assertEquals("test", viewModel.searchQuery.first())
    }

    @Test
    fun `clearExecutionResult clears the result`() = runTest {
        whenever(recurringRepository.getAllRules()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getActiveRules()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getAllTransactions()).thenReturn(flowOf(emptyList()))
        whenever(recurringRepository.getTransactionsBetweenDates(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(flowOf(emptyList()))

        viewModel = RecurringViewModel(recurringRepository, recurringEngine)
        viewModel.clearExecutionResult()

        assertFalse(viewModel.uiState.value.lastExecutionError.isNullOrEmpty() || viewModel.uiState.value.lastExecutionResult != null)
    }

    private fun createTestRule(id: Long, name: String) = RecurringRule(
        id = id,
        name = name,
        transactionType = TransactionType.EXPENSE,
        amount = 100.0,
        sourceAccount = com.rudra.smartworktracker.data.entity.AccountType.BALANCE,
        frequency = RecurringFrequency.MONTHLY,
        startDate = System.currentTimeMillis(),
        nextExecutionDate = System.currentTimeMillis()
    )

    private fun createTestTransaction(id: Long, scheduledDate: Long = System.currentTimeMillis()) = RecurringTransaction(
        id = id,
        ruleId = 1L,
        name = "Test Transaction",
        transactionType = TransactionType.EXPENSE,
        amount = 100.0,
        sourceAccount = com.rudra.smartworktracker.data.entity.AccountType.BALANCE,
        scheduledDate = scheduledDate,
        status = RecurringTransactionStatus.PENDING
    )
}
