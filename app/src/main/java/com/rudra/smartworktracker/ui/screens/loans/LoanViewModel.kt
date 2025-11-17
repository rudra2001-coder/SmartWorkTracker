package com.rudra.smartworktracker.ui.screens.loans

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Loan
import com.rudra.smartworktracker.data.entity.LoanType
import com.rudra.smartworktracker.data.repository.LoanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class LoansUiState(
    val loans: List<Loan> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showAddLoanDialog: Boolean = false,
    val showRepayDialogForLoan: Loan? = null
)

class LoanViewModel(private val loanRepository: LoanRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoansUiState())
    val uiState: StateFlow<LoansUiState> = _uiState.asStateFlow()

    init {
        loadLoans()
    }

    private fun loadLoans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            loanRepository.getAllLoans().catch { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }.collect { loans ->
                _uiState.value = _uiState.value.copy(
                    loans = loans,
                    isLoading = false
                )
            }
        }
    }

    fun addLoan(personName: String, amount: Double, loanType: LoanType, notes: String?) {
        viewModelScope.launch {
            val loan = Loan(
                personName = personName,
                initialAmount = amount,
                remainingAmount = amount,
                loanType = loanType,
                date = System.currentTimeMillis(),
                notes = notes
            )
            loanRepository.insertLoan(loan)
        }
    }

    fun repayLoan(loan: Loan, amount: Double) {
        viewModelScope.launch {
            loanRepository.repayLoan(loan, amount)
        }
    }

    fun receiveLoanRepayment(loan: Loan, amount: Double) {
        viewModelScope.launch {
            loanRepository.receiveLoanRepayment(loan, amount)
        }
    }

    fun openAddLoanDialog() {
        _uiState.value = _uiState.value.copy(showAddLoanDialog = true)
    }

    fun closeAddLoanDialog() {
        _uiState.value = _uiState.value.copy(showAddLoanDialog = false)
    }

    fun openRepayDialog(loan: Loan) {
        _uiState.value = _uiState.value.copy(showRepayDialogForLoan = loan)
    }

    fun closeRepayDialog() {
        _uiState.value = _uiState.value.copy(showRepayDialogForLoan = null)
    }
}
