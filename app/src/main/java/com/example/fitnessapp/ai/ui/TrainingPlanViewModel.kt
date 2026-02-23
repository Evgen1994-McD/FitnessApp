package com.example.fitnessapp.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.ai.data.TrainingPlanRepository
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
    private val trainingPlanRepository: TrainingPlanRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TrainingPlanUiState())
    val uiState: StateFlow<TrainingPlanUiState> = _uiState.asStateFlow()
    
    private val _activePlan = MutableStateFlow<TrainingPlanModel?>(null)
    val activePlan: StateFlow<TrainingPlanModel?> = _activePlan.asStateFlow()
    
    private val _plannedDays = MutableStateFlow<List<PlannedDayModel>>(emptyList())
    val plannedDays: StateFlow<List<PlannedDayModel>> = _plannedDays.asStateFlow()
    
    private val _allPlans = MutableStateFlow<List<TrainingPlanModel>>(emptyList())
    val allPlans: StateFlow<List<TrainingPlanModel>> = _allPlans.asStateFlow()
    
    private val _recommendations = MutableStateFlow<List<ExerciseModel>>(emptyList())
    val recommendations: StateFlow<List<ExerciseModel>> = _recommendations.asStateFlow()
    
    init {
        loadAllPlans()
        loadActivePlan()
    }
    
    fun generatePlan(
        goal: String,
        targetZones: List<String>,
        availableDays: List<Int>,
        timePerSession: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val planId = trainingPlanRepository.generateAndSavePlan(
                    goal = goal,
                    targetZones = targetZones,
                    availableDays = availableDays,
                    timePerSession = timePerSession
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    success = "План успешно создан!"
                )
                
                loadAllPlans()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка создания плана: ${e.message}"
                )
            }
        }
    }
    
    fun activatePlan(planId: Int) {
        viewModelScope.launch {
            try {
                trainingPlanRepository.activatePlan(planId)
                loadActivePlan()
                _uiState.value = _uiState.value.copy(success = "План активирован!")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка активации: ${e.message}")
            }
        }
    }
    
    fun deletePlan(planId: Int) {
        viewModelScope.launch {
            try {
                trainingPlanRepository.deletePlan(planId)
                loadAllPlans()
                loadActivePlan()
                _uiState.value = _uiState.value.copy(success = "План удален!")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка удаления: ${e.message}")
            }
        }
    }
    
    fun loadRecommendations(targetZone: String, excludeIds: List<Int> = emptyList()) {
        viewModelScope.launch {
            try {
                val recommendations = trainingPlanRepository.getExerciseRecommendations(
                    targetZone = targetZone,
                    excludeIds = excludeIds
                )
                _recommendations.value = recommendations
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка загрузки рекомендаций: ${e.message}")
            }
        }
    }
    
    private fun loadAllPlans() {
        viewModelScope.launch {
            trainingPlanRepository.getAllPlans().collect { plans ->
                _allPlans.value = plans
            }
        }
    }
    
    private fun loadActivePlan() {
        viewModelScope.launch {
            val plan = trainingPlanRepository.getActivePlan()
            _activePlan.value = plan
            
            if (plan != null) {
                val days = trainingPlanRepository.getPlannedDays(plan.id ?: 0)
                _plannedDays.value = days
            } else {
                _plannedDays.value = emptyList()
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, success = null)
    }
}

data class TrainingPlanUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)
