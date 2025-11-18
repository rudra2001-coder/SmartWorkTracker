package com.rudra.smartworktracker.ui.screens.user_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.entity.UserProfile
import com.rudra.smartworktracker.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Sealed class for UI state management
sealed class UserProfileState {
    data object Loading : UserProfileState()
    data class Success(val userProfile: UserProfile) : UserProfileState()
    data class Error(val message: String) : UserProfileState()
    data object Empty : UserProfileState()
}

// Data class for form validation
data class UserProfileFormState(
    val name: String = "",
    val monthlySalary: String = "",
    val initialSavings: String = "",
    val nameError: String? = null,
    val salaryError: String? = null,
    val savingsError: String? = null,
    val isFormValid: Boolean = false
)

class UserProfileViewModel(private val repository: UserProfileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val uiState: StateFlow<UserProfileState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(UserProfileFormState())
    val formState: StateFlow<UserProfileFormState> = _formState.asStateFlow()

    private val _saveResult = MutableSharedFlow<SaveResult>()
    val saveResult: SharedFlow<SaveResult> = _saveResult.asSharedFlow()

    private val _userProfileData = MutableStateFlow<UserProfile?>(null)
    val userProfileData: StateFlow<UserProfile?> = _userProfileData.asStateFlow()

    // Backing property for the repository flow
    val userProfile: Flow<UserProfile?> = repository.userProfile

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userProfile
                .catch { exception ->
                    _uiState.value = UserProfileState.Error("Failed to load profile: ${exception.message}")
                }
                .collect { profile ->
                    if (profile != null) {
                        _userProfileData.value = profile
                        _uiState.value = UserProfileState.Success(profile)
                        // Initialize form state with existing profile data
                        updateFormState(
                            name = profile.name,
                            monthlySalary = profile.monthlySalary.toString(),
                            initialSavings = profile.initialSavings.toString()
                        )
                    } else {
                        _uiState.value = UserProfileState.Empty
                    }
                }
        }
    }

    fun updateFormState(
        name: String = _formState.value.name,
        monthlySalary: String = _formState.value.monthlySalary,
        initialSavings: String = _formState.value.initialSavings
    ) {
        val nameError = validateName(name)
        val salaryError = validateSalary(monthlySalary)
        val savingsError = validateSavings(initialSavings)

        val isFormValid = nameError == null && salaryError == null && savingsError == null

        _formState.value = UserProfileFormState(
            name = name,
            monthlySalary = monthlySalary,
            initialSavings = initialSavings,
            nameError = nameError,
            salaryError = salaryError,
            savingsError = savingsError,
            isFormValid = isFormValid
        )
    }

    fun saveUserProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            // Validate the profile before saving
            val validationResult = validateUserProfile(userProfile)
            if (!validationResult.isValid) {
                _saveResult.emit(SaveResult.Error(validationResult.errorMessage ?: "Invalid profile data"))
                return@launch
            }

            try {
                _saveResult.emit(SaveResult.Loading)
                repository.saveUserProfile(userProfile)
                _saveResult.emit(SaveResult.Success)

                // Update local state
                _userProfileData.value = userProfile

            } catch (e: Exception) {
                _saveResult.emit(SaveResult.Error("Failed to save profile: ${e.message}"))
            }
        }
    }

    // Convenience method to save from form data
    fun saveUserProfileFromForm(
        name: String,
        monthlySalary: String,
        initialSavings: String,
        salaryPeriod: com.rudra.smartworktracker.data.entity.SalaryPeriod,
        language: com.rudra.smartworktracker.data.entity.Language
    ) {
        val salary = monthlySalary.toDoubleOrNull() ?: 0.0
        val savings = initialSavings.toDoubleOrNull() ?: 0.0

        val userProfile = UserProfile(
            name = name.trim(),
            monthlySalary = salary,
            initialSavings = savings,
            salaryPeriod = salaryPeriod,
            language = language
        )

        saveUserProfile(userProfile)
    }

    fun clearSaveResult() {
        viewModelScope.launch {
            _saveResult.emit(SaveResult.Idle)
        }
    }

    // Validation methods
    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name cannot be empty"
            name.length < 2 -> "Name must be at least 2 characters"
            name.length > 50 -> "Name must be less than 50 characters"
            else -> null
        }
    }

    private fun validateSalary(salary: String): String? {
        return when {
            salary.isBlank() -> "Salary cannot be empty"
            salary.toDoubleOrNull() == null -> "Please enter a valid number"
            salary.toDouble() < 0 -> "Salary cannot be negative"
            salary.toDouble() > 1_000_000_000 -> "Salary is too large"
            else -> null
        }
    }

    private fun validateSavings(savings: String): String? {
        return when {
            savings.isBlank() -> "Savings cannot be empty"
            savings.toDoubleOrNull() == null -> "Please enter a valid number"
            savings.toDouble() < 0 -> "Savings cannot be negative"
            savings.toDouble() > 10_000_000_000 -> "Savings amount is too large"
            else -> null
        }
    }

    private fun validateUserProfile(userProfile: UserProfile): ValidationResult {
        return when {
            userProfile.name.isBlank() -> ValidationResult(false, "Name cannot be empty")
            userProfile.monthlySalary < 0 -> ValidationResult(false, "Salary cannot be negative")
            userProfile.initialSavings < 0 -> ValidationResult(false, "Savings cannot be negative")
            userProfile.name.length < 2 -> ValidationResult(false, "Name is too short")
            userProfile.name.length > 50 -> ValidationResult(false, "Name is too long")
            else -> ValidationResult(true)
        }
    }

    // Refresh profile data
    fun refreshProfile() {
        _uiState.value = UserProfileState.Loading
        loadUserProfile()
    }
}

// Result classes for save operations
sealed class SaveResult {
    data object Idle : SaveResult()
    data object Loading : SaveResult()
    data object Success : SaveResult()
    data class Error(val message: String) : SaveResult()
}

// Validation result data class
data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null)