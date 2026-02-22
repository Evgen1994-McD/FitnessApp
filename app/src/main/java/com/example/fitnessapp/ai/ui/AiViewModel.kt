package com.example.fitnessapp.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.ai.data.CactusAiRepository
import com.example.fitnessapp.ai.domain.models.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiRepository: CactusAiRepository
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()
    
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        viewModelScope.launch {
            // Добавляем сообщение пользователя
            val userMessage = ChatMessage(content = message, isUser = true)
            _messages.value += userMessage
            
            _isLoading.value = true
            
            try {
                // Получаем ответ AI
                val aiResponse = aiRepository.generateResponse(
                    com.example.fitnessapp.ai.domain.models.AiRequest(message = message)
                )
                
                val aiMessage = ChatMessage(content = aiResponse, isUser = false)
                _messages.value += aiMessage
                
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    content = "Ошибка: ${e.message}", 
                    isUser = false
                )
                _messages.value += errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateInputText(text: String) {
        _inputText.value = text
    }
    
    override fun onCleared() {
        super.onCleared()
        aiRepository.cleanup()
    }
}
