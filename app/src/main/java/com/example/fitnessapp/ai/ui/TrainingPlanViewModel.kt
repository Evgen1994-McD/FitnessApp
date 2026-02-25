package com.example.fitnessapp.ai.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.ai.data.TrainingPlanRepository
import com.example.fitnessapp.ai.domain.TrainingPlanAiService
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.PlannedDayModel
import com.example.fitnessapp.db.TrainingPlanModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingPlanViewModel @Inject constructor(
    private val trainingPlanRepository: TrainingPlanRepository,
    private val trainingPlanAiService: TrainingPlanAiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TrainingPlanUiState())
    val uiState: StateFlow<TrainingPlanUiState> = _uiState.asStateFlow()
    
    private val _activePlan = MutableStateFlow<TrainingPlanModel?>(null)
    val activePlan: StateFlow<TrainingPlanModel?> = _activePlan.asStateFlow()
    
    private val _plannedDays = MutableStateFlow<List<PlannedDayModel>>(emptyList())
    val plannedDays: StateFlow<List<PlannedDayModel>> = _plannedDays.asStateFlow()
    
    private val _allPlans = MutableStateFlow<List<TrainingPlanModel>>(emptyList())
    
    // Состояния диалога
    enum class DialogState {
        START, WAITING_FOR_ZONE, WAITING_FOR_DIFFICULTY, READY
    }
    
    private var dialogState = DialogState.START
    private var collectedZone: String? = null
    private var collectedDifficulty: String? = null
    
    fun processUserMessage(message: String, onResponse: (String) -> Unit) {
        viewModelScope.launch {
            Log.d("TrainingPlanViewModel", "📨 Сообщение от пользователя: '$message', состояние: $dialogState")
            
            val response = when (dialogState) {
                DialogState.START -> {
                    if (message.contains("план", ignoreCase = true) || 
                        message.contains("составь", ignoreCase = true) ||
                        message.contains("трениров", ignoreCase = true)) {
                        dialogState = DialogState.WAITING_FOR_ZONE
                        "Отлично! Я помогу составить план тренировок. 🏋️\n\nКакую зону хотите тренировать? Выберите из:\n• **руки**\n• **тело**\n• **спина**\n• **ноги**"
                    } else {
                        "Привет! Я могу составить персональный план тренировок. Напишите 'Составь план тренировки' чтобы начать."
                    }
                }
                
                DialogState.WAITING_FOR_ZONE -> {
                    val zone = detectZone(message)
                    if (zone != null) {
                        collectedZone = zone
                        dialogState = DialogState.WAITING_FOR_DIFFICULTY
                        "Отлично! Зона: ${getZoneDisplayName(zone)}. 💪\n\nТеперь какой уровень сложности? Выберите:\n• **начинающий**\n• **средний**\n• **продвинутый**"
                    } else {
                        "Пожалуйста, выберите зону из списка: руки, тело, спина, ноги. Напишите название зоны, которую хотите тренировать."
                    }
                }
                
                DialogState.WAITING_FOR_DIFFICULTY -> {
                    val difficulty = detectDifficulty(message)
                    if (difficulty != null) {
                        collectedDifficulty = difficulty
                        dialogState = DialogState.READY
                        
                        try {
                            // Создаем план БЕЗ AI - просто выбираем рандомные упражнения
                            Log.d("TrainingPlanViewModel", "🎯 Создаем план: зона=$collectedZone, сложность=$collectedDifficulty")
                            
                            _uiState.value = _uiState.value.copy(isLoading = true)
                            
                            // Получаем упражнения из БД и выбираем рандомные
                            val exercises = trainingPlanAiService.getExerciseRecommendations(
                                targetZone = collectedZone!!,
                                excludeIds = emptyList()
                            )
                            
                            val selectedExercises = exercises.shuffled().take(3)
                            Log.d("TrainingPlanViewModel", "📊 Выбраны упражнения: ${selectedExercises.map { "${it.id}:${it.name}" }}")
                            
                            // Создаем план вручную
                            val plan = com.example.fitnessapp.db.TrainingPlanModel(
                                name = "План тренировки на $collectedDifficulty",
                                description = "Персональный план для зоны ${getZoneDisplayName(collectedZone!!)}",
                                targetZones = collectedZone!!,
                                exercisesPerDay = 3,
                                aiGenerated = false, // Не AI генерация!
                                createdAt = System.currentTimeMillis(),
                                isActive = false
                            )
                            
                            // Сохраняем план
                            val planId = trainingPlanRepository.generateAndSavePlan(
                                goal = "тренировка $collectedDifficulty",
                                targetZones = listOf(collectedZone!!),
                                availableDays = listOf(1, 3, 5),
                                timePerSession = getTimeForDifficulty(difficulty)
                            )
                            
                            // Получаем сохраненные запланированные дни
                            val savedDays = trainingPlanRepository.getPlannedDays(planId.toInt())
                            _plannedDays.value = savedDays
                            
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isPlanCreated = true,
                                error = null
                            )
                            
                            "Отлично! ✅ План тренировки создан!\n\n🎯 **Зона**: ${getZoneDisplayName(collectedZone!!)}\n💪 **Сложность**: $difficulty\n⏱️ **Время**: ${getTimeForDifficulty(difficulty)} минут\n\n📋 **Упражнения в плане**:\n${selectedExercises.joinToString("\n") { "• ${it.name}" }}\n\nПлан готов! Вы можете начать тренировку. 🚀"
                            
                        } catch (e: Exception) {
                            Log.e("TrainingPlanViewModel", "❌ Ошибка создания плана: ${e.message}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Не удалось создать план: ${e.message}"
                            )
                            "Извините, произошла ошибка при создании плана. Попробуйте еще раз."
                        }
                    } else {
                        "Пожалуйста, выберите уровень сложности: начинающий, средний или продвинутый."
                    }
                }
                
                DialogState.READY -> {
                    "План уже создан! Хотите составить новый план? Напишите 'Составь новый план'."
                }
            }
            
            onResponse(response)
        }
    }
    
    private fun detectZone(message: String): String? {
        val lowerMessage = message.lowercase()
        return when {
            "рук" in lowerMessage -> "hands"
            "тел" in lowerMessage -> "body"
            "спин" in lowerMessage -> "back"
            "ног" in lowerMessage -> "legs"
            else -> null
        }
    }
    
    private fun detectDifficulty(message: String): String? {
        val lowerMessage = message.lowercase()
        return when {
            "начина" in lowerMessage -> "начинающий"
            "средн" in lowerMessage -> "средний"
            "продвин" in lowerMessage -> "продвинутый"
            else -> null
        }
    }
    
    private fun getZoneDisplayName(zone: String): String {
        return when (zone) {
            "hands" -> "руки"
            "body" -> "тело"
            "back" -> "спина"
            "legs" -> "ноги"
            else -> zone
        }
    }
    
    private fun getTimeForDifficulty(difficulty: String): Int {
        return when (difficulty) {
            "начинающий" -> 20
            "средний" -> 30
            "продвинутый" -> 45
            else -> 30
        }
    }
    
    // Сброс диалога для нового плана
    fun resetDialog() {
        dialogState = DialogState.START
        collectedZone = null
        collectedDifficulty = null
        _uiState.value = TrainingPlanUiState()
    }
    
    // Старые методы для совместимости
    fun generatePlan(goal: String, targetZones: List<String>, availableDays: List<Int>, timePerSession: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val planId = trainingPlanRepository.generateAndSavePlan(
                    goal = goal,
                    targetZones = targetZones,
                    availableDays = availableDays,
                    timePerSession = timePerSession
                )
                
                // Получаем сохраненные запланированные дни
                val savedDays = trainingPlanRepository.getPlannedDays(planId.toInt())
                _plannedDays.value = savedDays
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isPlanCreated = true
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class TrainingPlanUiState(
    val isLoading: Boolean = false,
    val isPlanCreated: Boolean = false,
    val error: String? = null
)
