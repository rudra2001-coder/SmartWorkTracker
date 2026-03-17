package com.rudra.smartworktracker.ui.screens.futureimpact

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.BehaviorEngineRepository
import com.rudra.smartworktracker.data.repository.DecisionRepository
import com.rudra.smartworktracker.model.CheckInType
import com.rudra.smartworktracker.model.ConsequenceDebt
import com.rudra.smartworktracker.model.DailyCheckIn
import com.rudra.smartworktracker.model.Decision
import com.rudra.smartworktracker.model.DecisionCategory
import com.rudra.smartworktracker.model.DecisionType
import com.rudra.smartworktracker.model.FutureIdentity
import com.rudra.smartworktracker.model.UserHistory
import com.rudra.smartworktracker.model.WeeklyReport
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class FutureImpactViewModel(
    private val behaviorRepo: BehaviorEngineRepository,
    private val decisionRepo: DecisionRepository,
    private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("future_self_prefs", Context.MODE_PRIVATE)
    
    private val _selectedIdentity = MutableStateFlow(loadIdentity())
    val selectedIdentity: StateFlow<FutureIdentity> = _selectedIdentity.asStateFlow()

    // Time-based data
    val todayDecisions: StateFlow<List<Decision>> = behaviorRepo.getDecisionsInRange(
        behaviorRepo.getTodayStart(), 
        System.currentTimeMillis()
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weekDecisions: StateFlow<List<Decision>> = behaviorRepo.getDecisionsInRange(
        behaviorRepo.get7DaysAgo(), 
        System.currentTimeMillis()
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthDecisions: StateFlow<List<Decision>> = behaviorRepo.getDecisionsInRange(
        behaviorRepo.get30DaysAgo(), 
        System.currentTimeMillis()
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Consequence Debt
    val debts: StateFlow<List<ConsequenceDebt>> = behaviorRepo.getAllDebts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val totalDebt: StateFlow<Float> = behaviorRepo.getTotalDebt()
        .map { it ?: 0f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // User History (Lock-in Effect)
    val userHistory: StateFlow<UserHistory?> = behaviorRepo.getUserHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Weekly Reports
    val latestReport: StateFlow<WeeklyReport?> = behaviorRepo.getLatestReport()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Daily Check-ins
    private val _morningCheckIn = MutableStateFlow<DailyCheckIn?>(null)
    val morningCheckIn: StateFlow<DailyCheckIn?> = _morningCheckIn.asStateFlow()
    
    private val _nightCheckIn = MutableStateFlow<DailyCheckIn?>(null)
    val nightCheckIn: StateFlow<DailyCheckIn?> = _nightCheckIn.asStateFlow()

    private val _showCheckInPrompt = MutableStateFlow(false)
    val showCheckInPrompt: StateFlow<Boolean> = _showCheckInPrompt.asStateFlow()

    // Impact Stats
    val impactStats: StateFlow<ImpactStats> = combine(
        weekDecisions,
        monthDecisions
    ) { week, month ->
        calculateImpact(week, month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ImpactStats())

    // Pattern Warnings
    val patternWarnings: StateFlow<List<PatternWarning>> = weekDecisions.map { decisions ->
        detectPatterns(decisions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Streaks
    val streaks: StateFlow<StreakInfo> = weekDecisions.map { decisions ->
        calculateStreaks(decisions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreakInfo())

    // Daily Score
    val dailyScore: StateFlow<Int> = combine(
        todayDecisions,
        streaks,
        impactStats
    ) { today, streakInfo, stats ->
        calculateDailyScore(today, streakInfo, stats)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50)

    // Immediate Feedback
    private val _immediateFeedback = MutableStateFlow<ImmediateFeedback?>(null)
    val immediateFeedback: StateFlow<ImmediateFeedback?> = _immediateFeedback.asStateFlow()

    // Undo Window
    private val _undoWindow = MutableStateFlow<UndoWindow?>(null)
    val undoWindow: StateFlow<UndoWindow?> = _undoWindow.asStateFlow()

    // Future Collapse Data
    val futureProjection: StateFlow<FutureProjection> = weekDecisions.map { decisions ->
        calculateFutureProjection(decisions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FutureProjection())

    // Check current time and show appropriate prompt
    init {
        viewModelScope.launch {
            behaviorRepo.initUserHistory()
            checkAndShowCheckIn()
        }
    }

    private fun checkAndShowCheckIn() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        _showCheckInPrompt.value = when {
            hour in 6..10 -> true  // Morning
            hour in 20..23 -> true // Night
            else -> false
        }
    }

    fun dismissCheckInPrompt() {
        _showCheckInPrompt.value = false
    }

    fun saveMorningCheckIn(mood: Int, answer: String) {
        viewModelScope.launch {
            val checkIn = DailyCheckIn(
                checkInType = CheckInType.MORNING,
                morningMood = mood,
                morningAnswer = answer
            )
            behaviorRepo.saveCheckIn(checkIn)
            _morningCheckIn.value = checkIn
            _showCheckInPrompt.value = false
        }
    }

    fun saveNightCheckIn(mood: Int, answer: String) {
        viewModelScope.launch {
            val checkIn = DailyCheckIn(
                checkInType = CheckInType.NIGHT,
                nightMood = mood,
                nightAnswer = answer
            )
            behaviorRepo.saveCheckIn(checkIn)
            _nightCheckIn.value = checkIn
            _showCheckInPrompt.value = false
        }
    }

    fun getCheckInType(): CheckInType? {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 6..10 -> CheckInType.MORNING
            hour in 20..23 -> CheckInType.NIGHT
            else -> null
        }
    }

    fun setIdentity(identity: FutureIdentity) {
        _selectedIdentity.value = identity
        prefs.edit().putString("future_identity", identity.name).apply()
    }

    private fun loadIdentity(): FutureIdentity {
        val name = prefs.getString("future_identity", FutureIdentity.NO_IDENTITY.name) 
            ?: FutureIdentity.NO_IDENTITY.name
        return try {
            FutureIdentity.valueOf(name)
        } catch (e: Exception) {
            FutureIdentity.NO_IDENTITY
        }
    }

    fun addDecision(decisionType: DecisionType, customTitle: String = "", notes: String = "") {
        viewModelScope.launch {
            val decision = Decision(
                decisionType = decisionType,
                customTitle = customTitle,
                notes = notes
            )
            behaviorRepo.addDecision(decision)
            showImmediateFeedback(decision)
            
            if (!decision.isPositive && decisionType.recoveryAction.isNotBlank()) {
                startUndoWindow(decision)
            }
        }
    }

    private fun showImmediateFeedback(decision: Decision) {
        val weekDecs = weekDecisions.value
        val type = decision.decisionType
        val countThisWeek = weekDecs.count { it.decisionType == type }
        
        _immediateFeedback.value = ImmediateFeedback(
            decisionType = type,
            timesThisWeek = countThisWeek,
            immediateConsequence = type.immediateConsequence,
            estimatedWeightChange = type.estimatedWeightChange * countThisWeek,
            energyImpact = type.energyImpact
        )
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(5000)
            _immediateFeedback.value = null
        }
    }

    private fun startUndoWindow(decision: Decision) {
        val type = decision.decisionType
        if (type.recoveryAction.isBlank()) return
        
        _undoWindow.value = UndoWindow(
            decision = decision,
            recoveryAction = type.recoveryAction,
            recoveryAction2 = type.recoveryAction2,
            expiresAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30)
        )
    }

    fun executeRecovery() {
        val undo = _undoWindow.value ?: return
        
        viewModelScope.launch {
            val recoveryType = getRecoveryType(undo.recoveryAction)
            val recoveryDecision = Decision(
                decisionType = recoveryType,
                notes = "Recovery from ${undo.decision.decisionType.displayName}"
            )
            behaviorRepo.addDecision(recoveryDecision)
            // Reduce debt for the recovery
            undo.decision.decisionType.category.let { category ->
                behaviorRepo.reduceDebt(category, 5f)
            }
            _undoWindow.value = null
        }
    }

    private fun getRecoveryType(action: String): DecisionType = when {
        action.contains("pushup", ignoreCase = true) || 
        action.contains("walk", ignoreCase = true) ||
        action.contains("exercise", ignoreCase = true) -> DecisionType.EXERCISE
        action.contains("water", ignoreCase = true) ||
        action.contains("protein", ignoreCase = true) -> DecisionType.EAT_HEALTHY
        action.contains("nap", ignoreCase = true) ||
        action.contains("sleep", ignoreCase = true) -> DecisionType.SLEEP_EARLY
        action.contains("transfer", ignoreCase = true) ||
        action.contains("save", ignoreCase = true) -> DecisionType.SAVE_MONEY
        action.contains("task", ignoreCase = true) ||
        action.contains("start", ignoreCase = true) -> DecisionType.WORK_FOCUS
        action.contains("breath", ignoreCase = true) ||
        action.contains("grateful", ignoreCase = true) -> DecisionType.MEDITATE
        action.contains("text", ignoreCase = true) ||
        action.contains("call", ignoreCase = true) -> DecisionType.MEET_FRIENDS
        action.contains("screen", ignoreCase = true) ||
        action.contains("phone", ignoreCase = true) -> DecisionType.MEDITATE
        action.contains("plan", ignoreCase = true) ||
        action.contains("read", ignoreCase = true) -> DecisionType.SET_GOALS
        else -> DecisionType.WORK_FOCUS
    }

    fun dismissUndoWindow() {
        _undoWindow.value = null
    }

    fun deleteDecision(id: String) {
        viewModelScope.launch {
            behaviorRepo.deleteDecision(id)
        }
    }

    fun generateWeeklyReport() {
        viewModelScope.launch {
            val weekStart = behaviorRepo.getWeekStart()
            val weekEnd = System.currentTimeMillis()
            val decisions = weekDecisions.value
            val avgScore = dailyScore.value
            val streak = streaks.value
            
            behaviorRepo.generateWeeklyReport(
                weekStart = weekStart,
                weekEnd = weekEnd,
                decisions = decisions,
                avgScore = avgScore,
                identityAlignment = calculateIdentityAlignment(),
                disciplineStreak = streak.maxDisciplineStreak,
                damageStreak = streak.maxDamageStreak
            )
        }
    }

    private fun calculateImpact(weekDecisions: List<Decision>, monthDecisions: List<Decision>): ImpactStats {
        val weekPositive = weekDecisions.count { it.isPositive }
        val weekNegative = weekDecisions.count { !it.isPositive }

        val monthPositive = monthDecisions.count { it.isPositive }
        val monthNegative = monthDecisions.count { !it.isPositive }

        return ImpactStats(
            weekPositive = weekPositive,
            weekNegative = weekNegative,
            weekTotalImpact = weekDecisions.sumOf { it.decisionType.defaultImpact.toInt() },
            monthPositive = monthPositive,
            monthNegative = monthNegative,
            monthTotalImpact = monthDecisions.sumOf { it.decisionType.defaultImpact.toInt() },
            weekDecisionsByCategory = DecisionCategory.entries.associateWith { 
                weekDecisions.count { d -> d.decisionType.category == it } 
            },
            monthDecisionsByCategory = DecisionCategory.entries.associateWith { 
                monthDecisions.count { d -> d.decisionType.category == it } 
            }
        )
    }

    private fun calculateStreaks(decisions: List<Decision>): StreakInfo {
        if (decisions.isEmpty()) return StreakInfo()

        val sortedDecisions = decisions.sortedByDescending { it.createdAt }
        
        var maxDiscipline = 0
        var maxDamage = 0
        var tempDiscipline = 0
        var tempDamage = 0
        var currentType: Boolean? = null
        var currentStreak = 0
        
        for (decision in sortedDecisions) {
            if (decision.isPositive) {
                tempDiscipline++
                tempDamage = 0
                maxDiscipline = maxOf(maxDiscipline, tempDiscipline)
                if (currentType == true) {
                    currentStreak++
                } else {
                    currentType = true
                    currentStreak = 1
                }
            } else {
                tempDamage++
                tempDiscipline = 0
                maxDamage = maxOf(maxDamage, tempDamage)
                if (currentType == false) {
                    currentStreak++
                } else {
                    currentType = false
                    currentStreak = 1
                }
            }
        }

        return StreakInfo(
            currentDisciplineStreak = if (currentType == true) currentStreak else 0,
            currentDamageStreak = if (currentType == false) currentStreak else 0,
            maxDisciplineStreak = maxDiscipline,
            maxDamageStreak = maxDamage,
            isOnDamageStreak = maxDamage >= 2,
            isOnDisciplineStreak = maxDiscipline >= 2
        )
    }

    private fun calculateDailyScore(todayDecisions: List<Decision>, streaks: StreakInfo, stats: ImpactStats): Int {
        if (todayDecisions.isEmpty()) return 50

        var score = 50
        val positiveRatio = todayDecisions.count { it.isPositive }.toFloat() / todayDecisions.size
        score += ((positiveRatio - 0.5f) * 40).toInt()
        
        if (streaks.isOnDamageStreak) score -= streaks.currentDamageStreak * 5
        if (streaks.isOnDisciplineStreak) score += streaks.currentDisciplineStreak * 5
        score += ((stats.weekBalance - 50) * 0.3f).toInt()
        
        score += calculateIdentityAlignment()
        
        return score.coerceIn(0, 100)
    }

    private fun calculateIdentityAlignment(): Int {
        val identity = _selectedIdentity.value
        if (identity == FutureIdentity.NO_IDENTITY) return 0
        
        val today = todayDecisions.value
        if (today.isEmpty()) return 0
        
        val alignedCount = today.count { it.decisionType.category in identity.targetCategories }
        return if (today.isNotEmpty()) (alignedCount.toFloat() / today.size * 10).toInt() else 0
    }

    private fun detectPatterns(decisions: List<Decision>): List<PatternWarning> {
        val warnings = mutableListOf<PatternWarning>()
        val streak = streaks.value
        
        val negativeCount = decisions.count { !it.isPositive }
        val positiveCount = decisions.count { it.isPositive }

        if (negativeCount > positiveCount * 2 && negativeCount >= 3) {
            warnings.add(PatternWarning(
                title = "Negative Pattern Detected",
                description = "You've made $negativeCount negative decisions vs $positiveCount positive this week.",
                severity = PatternSeverity.HIGH,
                advice = "Focus on making at least one positive decision per day."
            ))
        }

        if (streak.currentDamageStreak >= 3) {
            warnings.add(PatternWarning(
                title = "⚠️ POINT OF NO RETURN",
                description = "You're on a $streak.currentDamageStreak-day damage streak. Recovery gets harder each day.",
                severity = PatternSeverity.HIGH,
                advice = "If you continue 2 more days, habits become permanent. ACT NOW to break the cycle!"
            ))
        }

        val categoryCounts = decisions.groupBy { it.decisionType.category }
        val worstCategory = categoryCounts.maxByOrNull { it.value.size }?.key
        val worstCount = categoryCounts[worstCategory]?.size ?: 0

        if (worstCategory != null && worstCount >= 3) {
            val impact = when (worstCategory) {
                DecisionCategory.HEALTH -> "Your health is suffering."
                DecisionCategory.FINANCE -> "Financial decisions are hurting your future."
                DecisionCategory.PRODUCTIVITY -> "Productivity is declining."
                DecisionCategory.SOCIAL -> "Social isolation increases."
                DecisionCategory.MENTAL_HEALTH -> "Stress is accumulating."
                DecisionCategory.PERSONAL -> "Personal growth is stalled."
                DecisionCategory.OTHER -> "Negative patterns in daily decisions."
            }
            warnings.add(PatternWarning(
                title = "${worstCategory.displayName} Crisis",
                description = "$worstCount decisions in ${worstCategory.displayName} this week.",
                severity = if (worstCount >= 5) PatternSeverity.HIGH else PatternSeverity.MEDIUM,
                advice = impact
            ))
        }

        return warnings
    }

    private fun calculateFutureProjection(decisions: List<Decision>): FutureProjection {
        if (decisions.isEmpty()) {
            return FutureProjection(
                currentScore = 50,
                week1Score = 50,
                week2Score = 50,
                week3Score = 50,
                week4Score = 50,
                trend = Trend.STABLE,
                message = "No data yet. Start logging decisions to see your future."
            )
        }

        val positiveRatio = decisions.count { it.isPositive }.toFloat() / decisions.size
        val currentScore = (positiveRatio * 100).toInt()
        
        // Calculate trend based on recent decisions
        val recentDecisions = decisions.take(10)
        val recentPositiveRatio = recentDecisions.count { it.isPositive }.toFloat() / recentDecisions.size
        
        val trend = when {
            recentPositiveRatio > 0.6f -> Trend.IMPROVING
            recentPositiveRatio < 0.4f -> Trend.DECLINING
            else -> Trend.STABLE
        }

        // Project future scores
        val improvement = (recentPositiveRatio - 0.5f) * 5
        val week1Score = (currentScore + improvement).coerceIn(0f, 100f).toInt()
        val week2Score = (week1Score + improvement * 0.8f).coerceIn(0f, 100f).toInt()
        val week3Score = (week2Score + improvement * 0.6f).coerceIn(0f, 100f).toInt()
        val week4Score = (week3Score + improvement * 0.4f).coerceIn(0f, 100f).toInt()

        val message = when (trend) {
            Trend.IMPROVING -> "📈 Great trajectory! If you continue, your future self will thank you."
            Trend.DECLINING -> "📉 Concerning path. Without change, your future shows significant decline."
            Trend.STABLE -> "➡️ You're stable but not growing. Small improvements compound over time."
        }

        return FutureProjection(
            currentScore = currentScore,
            week1Score = week1Score,
            week2Score = week2Score,
            week3Score = week3Score,
            week4Score = week4Score,
            trend = trend,
            message = message
        )
    }

    fun predictFuture(decisions: List<Decision>): String {
        if (decisions.isEmpty()) return "No data to predict future."

        val positiveRatio = decisions.count { it.isPositive }.toFloat() / decisions.size
        
        return when {
            positiveRatio >= 0.7f -> "🌟 Excellent! Building habits that compound."
            positiveRatio >= 0.5f -> "📈 Good direction! Steady progress ahead."
            positiveRatio >= 0.3f -> "⚠️ Concerning: Negative outweighs positive."
            else -> "🚨 Critical: Destructive patterns. WAKE-UP CALL!"
        }
    }

    fun getIdentityMessage(): String {
        val identity = _selectedIdentity.value
        if (identity == FutureIdentity.NO_IDENTITY) return ""
        
        val todayDecisions = todayDecisions.value
        val alignedCount = todayDecisions.count { it.decisionType.category in identity.targetCategories }
        
        if (todayDecisions.isEmpty()) return ""
        
        val isAligned = alignedCount > 0
        return if (isAligned) identity.positivePhrase else identity.negativePhrase
    }
}

// Data classes
data class ImpactStats(
    val weekPositive: Int = 0,
    val weekNegative: Int = 0,
    val weekTotalImpact: Int = 0,
    val monthPositive: Int = 0,
    val monthNegative: Int = 0,
    val monthTotalImpact: Int = 0,
    val weekDecisionsByCategory: Map<DecisionCategory, Int> = emptyMap(),
    val monthDecisionsByCategory: Map<DecisionCategory, Int> = emptyMap()
) {
    val weekNetImpact: Int get() = weekPositive - weekNegative
    val monthNetImpact: Int get() = monthPositive - monthNegative
    val weekBalance: Float get() = if (weekPositive + weekNegative > 0) 
        (weekPositive.toFloat() / (weekPositive + weekNegative)) * 100 else 50f
    val monthBalance: Float get() = if (monthPositive + monthNegative > 0) 
        (monthPositive.toFloat() / (monthPositive + monthNegative)) * 100 else 50f
}

data class StreakInfo(
    val currentDisciplineStreak: Int = 0,
    val currentDamageStreak: Int = 0,
    val maxDisciplineStreak: Int = 0,
    val maxDamageStreak: Int = 0,
    val isOnDamageStreak: Boolean = false,
    val isOnDisciplineStreak: Boolean = false
)

data class ImmediateFeedback(
    val decisionType: DecisionType,
    val timesThisWeek: Int,
    val immediateConsequence: String,
    val estimatedWeightChange: Float,
    val energyImpact: Int
)

data class UndoWindow(
    val decision: Decision,
    val recoveryAction: String,
    val recoveryAction2: String,
    val expiresAt: Long
)

data class PatternWarning(
    val title: String,
    val description: String,
    val severity: PatternSeverity,
    val advice: String
)

data class FutureProjection(
    val currentScore: Int = 50,
    val week1Score: Int = 50,
    val week2Score: Int = 50,
    val week3Score: Int = 50,
    val week4Score: Int = 50,
    val trend: Trend = Trend.STABLE,
    val message: String = ""
)

enum class Trend { IMPROVING, DECLINING, STABLE }

enum class PatternSeverity { LOW, MEDIUM, HIGH }

class FutureImpactViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FutureImpactViewModel::class.java)) {
            val db = AppDatabase.getDatabase(application)
            val behaviorRepo = BehaviorEngineRepository(
                db.decisionDao(),
                db.checkInDao(),
                db.consequenceDebtDao(),
                db.weeklyReportDao(),
                db.userHistoryDao()
            )
            val decisionRepo = DecisionRepository(db.decisionDao())
            @Suppress("UNCHECKED_CAST")
            return FutureImpactViewModel(behaviorRepo, decisionRepo, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
