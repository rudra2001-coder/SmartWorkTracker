package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rudra.smartworktracker.data.entity.BaseEntity
import com.rudra.smartworktracker.data.entity.SyncStatus

@Entity(tableName = "health_metrics")
data class HealthMetric(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uuid: String? = null,
    val type: HealthMetricType,
    val value: Double,
    val secondaryValue: Double? = null,
    val timestamp: Long,
    val notes: String? = null,
    val tags: String? = null,
    val isGoalAchieved: Boolean = false,
    val mood: MoodType? = null,
    val stressLevel: Int? = null,
    val duration: Long? = null,
    
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity

enum class HealthMetricType {
    WEIGHT,
    HEIGHT,
    WATER,
    SLEEP,
    SLEEP_QUALITY,
    BREAKS,
    EXERCISE,
    EXERCISE_TYPE,
    SCREEN_TIME,
    POSTURE,
    FOCUS,
    CALORIES,
    PROTEIN,
    CARBS,
    FIBER,
    FAT,
    HEART_RATE,
    BLOOD_PRESSURE,
    BLOOD_OXYGEN,
    STEPS,
    MOOD,
    STRESS,
    MEDITATION,
    MOISTURIZER,
    SUNSCREEN,
    SKIN_CARE,
    HAIR_CARE,
    WATER_PH,
    BODY_TEMPERATURE,
    WAKE_UP_TIME,
    BED_TIME,
    MOOD_NOTES
}

enum class MoodType(val emoji: String, val label: String) {
    EXCELLENT("😄", "Excellent"),
    GOOD("🙂", "Good"),
    NEUTRAL("😐", "Neutral"),
    BAD("😔", "Bad"),
    TERRIBLE("😢", "Terrible"),
    ENERGETIC("⚡", "Energetic"),
    TIRED("😴", "Tired"),
    ANXIOUS("😰", "Anxious"),
    CALM("😌", "Calm"),
    STRESSED("😤", "Stressed")
}

enum class ExerciseType(val label: String) {
    WALKING("Walking"),
    RUNNING("Running"),
    CYCLING("Cycling"),
    SWIMMING("Swimming"),
    YOGA("Yoga"),
    GYM("Gym"),
    STRETCHING("Stretching"),
    HIIT("HIIT"),
    SPORTS("Sports"),
    OTHER("Other")
}

enum class BodyPart(val label: String) {
    FACE("Face"),
    HANDS("Hands"),
    ARMS("Arms"),
    LEGS("Legs"),
    BODY("Body"),
    FULL_BODY("Full Body")
}
